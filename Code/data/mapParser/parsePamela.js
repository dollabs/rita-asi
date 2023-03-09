const fs = require("fs");
const dataPath = "./testingData/Spiral-4-Saturn_2.6.metadata";
const getFuncNames = false;
const getResults = true;
const maxX = -2087;
const minX = -2225;
const maxZ = 61;
const minZ = -11;

/* This file will generate the following Pamela syntax:
 * 1. lvar connection:                M1-SCE	 (lvar "M1 Storage T-Storage Corridor East")	;;door
 * 2. Door, Opening, Extension:       doorID	 (Door -2091 42 60 -2090 43 61 "c_110_-33_111_-32" M1-SCE)
 * 3. Room, Treatment Area, Hallway:
 *      A4A_2	(Room2Conn -2220 -9 60 -2212 -8 61 "A4A The King's Terrace" A4A_1-A4A_2 A4A_2-A4A_3)
 *      TAAN	(Treatment1Conn -2194 -5 60 -2181 -2 61 "North Zone A  Abrasion" TAAN-CCE)
 *      MCW	  (MainCWest -2215 55 60 -2158 60 61 "Main Corridor West" A1-MCW MCW-EL_3 CCW-MCW MCW-CCE MCW-CA MCW-TABS)
 * 4. Irrelevant Pamela functions
 **/

// ---------------------------------------------------;;
//                 Supporting Functions               ;;
//----------------------------------------------------;;
const getMapJson = (filePath) => {
  return new Promise((resolve, reject) => {
    fs.readFile(filePath, "utf8", (err, rawData) => {
      if (err) reject(err);
      const mapData = rawData.split("\n").find((data) => {
        const jsonData = JSON.parse(data);
        return jsonData.data.semantic_map;
      });
      const semanticMapJSON = JSON.parse(mapData);
      resolve(semanticMapJSON.data.semantic_map);
    });
  });
};

const getRoomsAndHallways = (objectsArray) => {
  return new Promise((resolve, _reject) => {
    const roomsAndHallways = {}; // rooms, hallway

    objectsArray.forEach((location) => {
      const { type, id, bounds } = location;
      if (bounds) {
        let x1 = bounds?.coordinates[0]?.x;
        let z1 = bounds?.coordinates[0]?.z;
        let x2 = bounds?.coordinates[1]?.x;
        let z2 = bounds?.coordinates[1]?.z;

        // This line does not work for "parts", only whole rooms
        if (location.name === "UNKNOWN") return;

        if (
          (x1 >= minX && x2 <= maxX && z1 >= minZ && z2 <= maxZ) ||
          // Special cases: Entrance Walkway has 65 <= Z <= 109, which is out of bounds, however, we want to include it.
          id === "ew" ||
          type === "treatment"
        ) {
          // Only add rooms and hallways located inside a specified area
          let name = location.name.replace("Part of ", "");
          roomsAndHallways[id.toUpperCase()] = {
            name,
            type,
            bounds: { x1, z1, x2, z2 },
          };
        }
      }

      resolve(roomsAndHallways);
    });
  });
};

const addPamelaToMap = (
  key,
  functionName,
  boundsString,
  name,
  connectionsKey,
  pamelaOutput,
  pamelaFunctions,
  funcType
) => {
  let argsConnId = "";
  let fields = "";
  let functionStr;
  const val = `${key}\t(${functionName} ${boundsString} "${name}" ${connectionsKey.join(
    " "
  )})`;

  for (let i = 1; i <= connectionsKey.length; i++) {
    argsConnId += ` connId${i}`;
    fields += `\n\t\tcx${i} connId${i}`;
  }

  if (funcType === "room") {
    functionStr = `(defpclass ${functionName} [botLeftX botLeftZ botLeftY topRightX topRightZ topRightY name ${argsConnId}]
      :meta {:doc "A ${connectionsKey.length} Connections Room"}
      :inherit [RectangularVolume]
      :modes {
        :unvisited (or (mode-of General :initial)) 
        :visited true 
      }
      :fields {${fields}})`;
  }

  if (funcType === "treatment") {
    functionStr = `(defpclass ${functionName} [botLeftX botLeftZ botLeftY topRightX topRightZ topRightY name ${argsConnId}]
      :meta {:doc "A ${connectionsKey.length} Connections Treatment Area"}
      :inherit [RectangularVolume]
      :modes {:unvisited (or (mode-of General :initial))
          :visited true
      }
      :fields {${fields}})`;
  }

  if (funcType === "corridor") {
    functionStr = `(defpclass ${functionName} [botLeftX botLeftZ botLeftY topRightX topRightZ topRightY name ${argsConnId}]
      :meta {:doc "A ${connectionsKey.length} Connections Corridor"}
      :inherit [RectangularVolume]
      :fields {${fields}})`;
  }

  if (!pamelaFunctions.get(functionName)) {
    pamelaFunctions.set(functionName, functionStr);
  }

  pamelaOutput.set(key, [val, connectionsKey.length]);
};

const isExtensionAndOutOfBound = (type, namesInPair, roomsAndHallways) => {
  const names = namesInPair.split("-").map((nameId) => {
    if (roomsAndHallways[nameId]) {
      return roomsAndHallways[nameId].name;
    }
    return false;
  });

  if (names[0] && names[1]) {
    return type !== "extension" && [...new Set(names)].length === 1
      ? false
      : names;
  }

  return false;
};

// -----------------------------------;;
//                 Main               ;;
// -----------------------------------;;
(async () => {
  try {
    const { connections, locations, _objects } = await getMapJson(dataPath);
    const pamelaLvarConnections = new Map();
    const pamelaDoorsAndOpenings = new Map();
    const pamelaRooms = new Map();
    const pamelaRoomFunctions = new Map();
    const pamelaHallways = new Map();
    const pamelaHallwayFunctions = new Map();
    const pamelaTreatments = new Map();
    const pamelaTreatmentFunctions = new Map();
    const allHallways = [];
    const roomsAndHallways = await getRoomsAndHallways(locations); // name, type, bound

    // Get unique function names for SE (volumes.clj)
    const allConnectionFncNames = new Set();
    const allRoomFncNames = new Set();
    const allTreatmentFncNames = new Set();
    const allCorridorFncNames = new Set();

    // Create (1) lvar connections, and (2) Door, Opening, Extension
    connections.forEach((connection) => {
      const { type, connected_locations, id, bounds } = connection;
      if (connected_locations.length <= 1) return;
      if (connection.passable === "false") {
        return;
      }

      // Create an array of lvar connections
      const rawLvarConnArr = connected_locations.flatMap((elem, i) =>
        connected_locations
          .slice(i + 1)
          .map(
            (innerArrElem) =>
              elem.toUpperCase() + "-" + innerArrElem.toUpperCase()
          )
      );
      // Filter out same-room connections (aka extensions) AND
      // Filter out connections outside of the specified bounds
      const finalLvarConnArr = [];

      for (let i = 0; i < rawLvarConnArr.length; i++) {
        names = isExtensionAndOutOfBound(
          type,
          rawLvarConnArr[i],
          roomsAndHallways
        );
        if (!names) continue;

        finalLvarConnArr.push(rawLvarConnArr[i]);
        // Store the final lvar syntax value to Map
        pamelaLvarConnections.set(
          rawLvarConnArr[i],
          `${rawLvarConnArr[i]}\t (lvar "${names.join("-")}")\t;;${type}`
        );
      }

      // Return if the door/opening/extension doesn't connect more than 2 areas
      if (finalLvarConnArr.length === 0) return;
      const bound1 = bounds.coordinates[0];
      const bound2 = bounds.coordinates[1];
      const boundsString = `${bound1.x} ${bound1.z} 60 ${bound2.x} ${bound2.z} 61`;

      let typeInPascalCase = type
        .split("_")
        .map((elem) => elem.charAt(0).toUpperCase() + elem.slice(1))
        .join("");

      for (let i = 0; i < finalLvarConnArr.length; i++) {
        const locations = finalLvarConnArr[i].split("-");
        if (
          roomsAndHallways[locations[0]].type.includes("hallway") &&
          roomsAndHallways[locations[1]].type.includes("hallway") &&
          type !== "extension"
        ) {
          typeInPascalCase = "CorridorJoin";
        }

        allConnectionFncNames.add(typeInPascalCase);

        // Store the final door/opening/extension value to Map
        const pamelaObjectsVal = `${id}_${i}\t\t(${typeInPascalCase} ${boundsString} "${id}" ${finalLvarConnArr[i]})`;
        pamelaDoorsAndOpenings.set(`${id}_${i}`, pamelaObjectsVal);
      }
    });

    // 3. Build rooms and hallways Pamela Objects
    for (const key in roomsAndHallways) {
      const { type, bounds, name } = roomsAndHallways[key];
      const boundsString = `${bounds.x1} ${bounds.z1} 60 ${bounds.x2} ${bounds.z2} 61`;
      let functionName;
      // Create String: all connections
      const connections = [...pamelaLvarConnections.entries()].filter(
        (connection) => connection[0].includes(key)
      );

      const connectionsKey = connections.map((conn) => conn[0]);

      // Construct the full pamela representation based on types: room vs treatment vs corridor
      // Room
      if (type.includes("room") || type.includes("bathroom")) {
        functionName = `Room${connections.length}Conn`;
        allRoomFncNames.add(functionName);
        addPamelaToMap(
          key,
          functionName,
          boundsString,
          name,
          connectionsKey,
          pamelaRooms,
          pamelaRoomFunctions,
          "room"
        );
      }
      // Treatment
      else if (type == "treatment") {
        functionName = `Treatment${connections.length}Conn`;
        allTreatmentFncNames.add(functionName);
        addPamelaToMap(
          key,
          functionName,
          boundsString,
          name,
          connectionsKey,
          pamelaTreatments,
          pamelaTreatmentFunctions,
          "treatment"
        );
      }
      // Hallways
      else {
        functionName = name.split(" ").join("");
        if (!functionName.includes("Corridor")) {
          functionName = key;
        } else {
          functionName = functionName.replace(/(\Corridor)/, "C") + `_${key}`;
        }
        allCorridorFncNames.add(functionName);
        allHallways.push(key);
        addPamelaToMap(
          key,
          functionName,
          boundsString,
          name,
          connectionsKey,
          pamelaHallways,
          pamelaHallwayFunctions,
          "corridor"
        );
      }
    }

    // ------- RESULT ------- //
    if (getResults) {
      // 1. Functions
      console.log("\n**** FUNCTIONS Room **** ");
      pamelaRoomFunctions.forEach((fnc) => {
        console.log(fnc);
      });

      console.log("\n**** FUNCTIONS Treatment Area **** ");
      pamelaTreatmentFunctions.forEach((fnc) => {
        console.log(fnc);
      });

      console.log("\n**** FUNCTIONS Hallway **** ");
      pamelaHallwayFunctions.forEach((fnc) => {
        console.log(fnc);
      });

      console.log("\n**** FUNCTION Building **** ");
      let argsConnId = "";
      let fields = "";
      let functionStr;
      for (let i = 1; i <= allHallways.length; i++) {
        argsConnId += ` connId${i}`;
        fields += `\n\t\tcorridor${i} connId${i}`;
      }

      functionStr = `(defpclass Building [${argsConnId}]
         :meta {:doc "The SAR building"}
         :modes [:electricity-on :no-power]
         :fields {${fields}})`;
      console.log(functionStr);

      if (getFuncNames) {
        console.log("\nConnections: ");
        console.log(
          [...allConnectionFncNames].map((fncName) => `'${fncName}`).join(" ")
        );

        console.log("\nRooms & Bathrooms: ");
        console.log(
          [...allRoomFncNames].map((fncName) => `'${fncName}`).join(" ")
        );

        console.log("\nTreatments: ");
        console.log(
          [...allTreatmentFncNames].map((fncName) => `'${fncName}`).join(" ")
        );

        console.log("\nCorridors: ");
        console.log(
          [...allCorridorFncNames].map((fncName) => `'${fncName}`).join(" ")
        );
      }

      // 2. Objects
      // ALL connectionID lvar
      console.log("\n**** Connection ****");
      pamelaLvarConnections.forEach((obj) => {
        console.log(obj);
      });

      // Doors + openings
      console.log("\n**** Door and Opening Objects **** ");
      pamelaDoorsAndOpenings.forEach((obj) => {
        console.log(obj);
      });

      // Rooms + Bathrooms
      console.log("\n**** Room Objects ****");
      pamelaRooms.forEach((obj) => {
        console.log(obj[0]);
      });

      // Treatment areas
      console.log("\n**** Treatment Area Objects ****");
      pamelaTreatments.forEach((obj) => {
        console.log(obj[0]);
      });

      // Hallways/Corridors Objects
      console.log("\n**** Hallway Objects ****");
      pamelaHallways.forEach((obj) => {
        console.log(obj[0]);
      });

      // Building
      console.log("\n**** Building Object ****");
      const buildingPamela = `BUILDING\t(Building  ${allHallways.join(" ")})`;
      console.log(buildingPamela);
    }
  } catch (err) {
    console.log(err.message, err.stack);
  }
})();
