/* eslint-disable prefer-destructuring */
/* eslint-disable guard-for-in */
/* eslint-disable no-shadow */
/* eslint-disable no-loop-func */
/* eslint-disable no-undef */
/* eslint-disable camelcase */
/* eslint-disable no-restricted-syntax */
const Phaser = require('phaser');
const Map = require('../map');

const red = '0xe06c78';
const green = '0x6aab9c';
const blue = '0x5874dc';
const selected_floor = 60;
let isFixedRoles;

// For old data, which players ain't assigned colors
const colorArr = [red, blue, green];
const playerArr = ['redPlayer', 'bluePlayer', 'greenPlayer'];
const isTesting = true;
/*--------------------------------*/
/*----- SUPPORTING FUNCTIONS -----*/
/*--------------------------------*/
function printMessage(isTest, message) {
  if (isTest) {
    console.log(message);
  }
}
// Adjust the x and z coordinates to reflect the selected map
function scaleXCoordinate(x, scene) {
  return Math.abs(x - scene.map_coordinates.x_left) * 16;
}

function scaleZCoordinate(z, scene) {
  return Math.abs(z - scene.map_coordinates.z_top) * 16;
}

// Player movement
function playerMovement(playerPos, playerN, color, playerImage, infoCoord, scene, isFixedRoles) {
  const movementsArr = playerPos;
  // Add graphics (to draw line) object to the scene, if not already
  if (scene.graphics[playerN] === null) {
    scene.graphics[playerN] = scene.add.graphics();
    scene.graphics[playerN].lineStyle(2, color, 1);
  }

  // Draw lines
  for (let i = 0; i < movementsArr.length - 1; i += 1) {
    const scale_x_i1 = scaleXCoordinate(movementsArr[i][0], scene);
    const scale_z_i1 = scaleZCoordinate(movementsArr[i][1], scene);
    const scale_x_i2 = scaleXCoordinate(movementsArr[i + 1][0], scene);
    const scale_z_i2 = scaleZCoordinate(movementsArr[i + 1][1], scene);
    // Draw the line that connects the last item from previous array to the first item in the current array)
    if (i === 0 && scene.connector[playerN].x !== 0 && scene.connector[playerN].z !== 0) {
      scene.graphics[playerN].lineBetween(scene.connector[playerN].x, scene.connector[playerN].z, scale_x_i1, scale_z_i1);
    }

    // Draw lines connecting all items within the current array
    scene.graphics[playerN].lineBetween(scale_x_i1, scale_z_i1, scale_x_i2, scale_z_i2);

    // Update the player's last coordinates
    if (i === movementsArr.length - 2) {
      scene.connector[playerN].x = scale_x_i2;
      scene.connector[playerN].z = scale_z_i2;

      // Add playerImage
      if (scene.spritePool.image_player[playerN] === null) {
        scene.spritePool.image_player[playerN] = scene.add.sprite(scale_x_i2, scale_z_i2, playerImage).setOrigin(0, 0);

        // Add roles' text
        if (!isFixedRoles) {
          if (scene.spritePool.image_player[`${playerN}Role`] === null) {
            scene.spritePool.image_player[`${playerN}Role`] = scene.add.sprite(infoCoord, 20, playerImage).setOrigin(0, 0);
          }
        }
      }
      // update playerImage position
      scene.spritePool.image_player[playerN].setPosition(scale_x_i2 - 16, scale_z_i2 - 16).setOrigin(0, 0);
    }
  }
}

function playerMovementColor(color, playerPos, playerN, infoCoord, scene, isFixedRoles) {
  if (color) {
    color = color.toLowerCase();
    if (color === 'red') {
      playerMovement(playerPos, playerN, red, 'redPlayer', infoCoord, scene, isFixedRoles);
    } else if (color === 'blue') {
      playerMovement(playerPos, playerN, blue, 'bluePlayer', infoCoord, scene, isFixedRoles);
    } else if (color === 'green') {
      playerMovement(playerPos, playerN, green, 'greenPlayer', infoCoord, scene, isFixedRoles);
    }
  } else {
    // For old
    const selectedColor = colorArr.pop();
    const selectedPlayer = playerArr.pop();
    playerMovement(playerPos, playerN, selectedColor, selectedPlayer, infoCoord, scene, isFixedRoles);
  }
}

// Add dynamic blockages
function addBlockage(data, scene, block_type, imageName) {
  data.forEach((blockage) => {
    let blockage_sprite = null;
    const scale_x = scaleXCoordinate(blockage.x, scene);
    const scale_z = scaleZCoordinate(blockage.z, scene);
    if (blockage.block_type === block_type) {
      printMessage(isTesting, `Info: added ${blockage.block_type}`);
      blockage_sprite = scene.add.sprite(scale_x, scale_z, imageName).setOrigin(0, 0);
    }
    scene.spritePool.image_blockage[`${scale_x},${scale_z}`] = blockage_sprite;
  });
}

// Initialized base map
function initializedBaseMap(map_type, scene, original_objs) {
  let basemap = null;

  if (map_type.toLowerCase().includes('sparky')) {
    scene.map_coordinates = Map.map_coordinates.Sparky;
    basemap = 'Sparky';
  } else if (map_type.toLowerCase().includes('falcon')) {
    scene.map_coordinates = Map.map_coordinates.Falcon;
    basemap = 'Falcon';
  } else if (map_type.toLowerCase().includes('saturn_1.1')) {
    scene.map_coordinates = Map.map_coordinates.Saturn1_1;
    basemap = 'Saturn1.1';
  } else if (map_type.toLowerCase().includes('saturn_1.6')) {
    scene.map_coordinates = Map.map_coordinates.Saturn1_6;
    basemap = 'Saturn1.6';
  } else if (map_type.toLowerCase().includes('saturn_2.0') || map_type.toLowerCase().includes('saturn_2.3')) {
    scene.map_coordinates = Map.map_coordinates.Saturn2_0;
    basemap = 'Saturn2.3';
  } else if (map_type.toLowerCase().includes('saturn_2.6') || map_type.toLowerCase().includes('saturn_2.5')) {
    scene.map_coordinates = Map.map_coordinates.Saturn2_6;
    basemap = 'Saturn2.6';
  }

  printMessage(isTesting, `'Base map selected:' ${basemap}, ${scene.map_coordinates}`);

  // Resize Canvas
  const adjusted_width = Math.abs(scene.map_coordinates.x_left - scene.map_coordinates.x_right) + 1;
  const adjusted_height = Math.abs(scene.map_coordinates.z_top - scene.map_coordinates.z_bottom) + 1;
  scene.scale.resize(adjusted_width * 16, adjusted_height * 16);

  // Destroy any pre-game objects
  original_objs.destroy();

  // Create the the tilemap based on the loaded json file
  const map = scene.make.tilemap({ key: `${basemap}` });

  // Create the tileset from png image
  const tileset = map.addTilesetImage('minecraft_blocks', 'tiles_minecraft'); // tileset name, tileset key

  // Render the map layer
  map.createStaticLayer('BaseMap', tileset);
}

/*------------------------------------*/
/*----- SCENE Object, named Game -----*/
/*------------------------------------*/
class Game extends Phaser.Scene {
  constructor() {
    super('game');

    // Variables of the current scene object, aka Game
    this.map_coordinates = null; // The coordinates of the selected map (Falcon || Saturn)
    this.room_coordinates = null; // Room coordinates (currently only apply to Falcon)

    this.connector = {
      // Stores the connecting point(Smoother UI - lines)
      player: { x: 0, z: 0 },
      player1: { x: 0, z: 0 },
      player2: { x: 0, z: 0 },
      player3: { x: 0, z: 0 },
      robot: { x: 0, z: 0 },
    };

    this.graphics = {
      // Stores the graphic object: line
      player: null,
      player1: null,
      player2: null,
      player3: null,
      robot: null,
    };

    this.graphics_room_prediction = null; // Stores the graphic object: shape (polygon) (for next-prediction)

    this.spritePool = {
      // { 'x,z' : sprite }
      image_robot: null,
      polygon_room: {},
      image_yellow_victim: {},
      image_green_victim: {},
      image_dead_victim: {},
      image_triaged_green_victim: {},
      image_triaged_yellow_victim: {},
      image_door_open: {},
      image_blockage: {},
      image_frozen: {},
      image_marker: {},
      image_player: {
        player: null,
        player1: null, // player image for movement (within the map)
        player1Role: null, // player image for the role section
        player2: null,
        player2Role: null,
        player3: null,
        player3Role: null,
      },
      text_role: {
        player1: null,
        player2: null,
        player3: null,
      },
      text_score: {
        player1: null,
        player2: null,
        player3: null,
        team: null,
      },
    };
  }

  destroyAndDeleteSprite(obj) {
    for (const key in obj) {
      obj[key].destroy(); // remove the sprite from the scene
      delete obj[key]; // Delete from the dictionary
    }
  }

  resetGame() {
    this.map_coordinates = null;
    this.room_coordinates = null;

    //  Movement points (Smoother UI - lines)
    for (const key in this.connector) {
      this.connector[key] = { x: 0, z: 0 };
    }

    // Line connecting points
    for (const key in this.graphics) {
      if (this.graphics[key] != null) {
        this.graphics[key].destroy();
        this.graphics[key] = null;
      }
    }

    if (this.graphics_room_prediction != null) {
      this.graphics_room_prediction.destroy();
      this.graphics_room_prediction = null;
    }

    // Sprite Pool
    if (this.spritePool.image_robot != null) {
      this.spritePool.image_robot.destroy();
      this.spritePool.image_robot = null;
    }

    this.destroyAndDeleteSprite(this.spritePool.polygon_room);
    this.destroyAndDeleteSprite(this.spritePool.image_yellow_victim);
    this.destroyAndDeleteSprite(this.spritePool.image_green_victim);
    this.destroyAndDeleteSprite(this.spritePool.image_dead_victim);
    this.destroyAndDeleteSprite(this.spritePool.image_triaged_green_victim);
    this.destroyAndDeleteSprite(this.spritePool.image_triaged_yellow_victim);
    this.destroyAndDeleteSprite(this.spritePool.image_door_open);
    this.destroyAndDeleteSprite(this.spritePool.image_blockage);
    this.destroyAndDeleteSprite(this.spritePool.image_frozen);
    this.destroyAndDeleteSprite(this.spritePool.image_marker);

    for (const key in this.spritePool.image_player) {
      if (this.spritePool.image_player[key] != null) {
        this.spritePool.image_player[key].destroy();
        this.spritePool.image_player[key] = null;
      }
    }

    for (const key in this.spritePool.text_role) {
      if (this.spritePool.text_role[key] != null) {
        this.spritePool.text_role[key].destroy();
        this.spritePool.text_role[key] = null;
      }
    }
    for (const key in this.spritePool.text_score) {
      if (this.spritePool.text_score[key] != null) {
        this.spritePool.text_score[key].destroy();
        this.spritePool.text_score[key] = null;
      }
    }
  }

  /*----- 1. Load assets before create() is called ------*/
  preload() {}

  /*----- 2. Add objects to the scene (with loaded assets) ------*/

  create() {
    //------- Local variables
    const scene = this; // Associate "this" to variable "scene" to avoid the keyword "this" of Socket
    let scale_x = 0;
    let scale_z = 0;

    //------- The initial display before adding the actual map
    const style = { font: 'bold 70px Arial', fill: '#303030' };
    const styleRole = { font: '23px Arial', fill: '#f8f7fc' };
    const input_text = scene.add.text(650, 500, 'Waiting for incoming data', style).setVisible(true);
    // initializedBaseMap('Saturn', scene, input_text);

    //------- DRAW MAP
    // Draw the base map from RabbitMQ input
    Map.socketFrontEnd.on(Map.socketKey.trial_start, function (data) {
      scene.resetGame(); // reset all sprite and graphics
      initializedBaseMap(data.mapName, scene, input_text);

      const trialData = data.data;
      // todo
      if (!trialData.isMultiPlayer) {
        return;
      }

      isFixedRoles = trialData.isFixedRoles;

      if (trialData.player1.callsign) {
        const player1_callsign = trialData.player1.callsign.toLowerCase();
        const player2_callsign = trialData.player2.callsign.toLowerCase();
        const player3_callsign = trialData.player3.callsign.toLowerCase();

        if (!isFixedRoles) {
          // Add player ID, playername, player role to the top of the 2D map
          scene.spritePool.image_player.player1Role = scene.add.sprite(500, 20, `${player1_callsign}Player`).setOrigin(0, 0); // callsign
          scene.add.text(550, 20, trialData.player1.participantid, styleRole);
          scene.add.text(500, 60, 'Name:', styleRole);
          scene.add.text(580, 60, trialData.player1.playername, styleRole);
          scene.add.text(500, 100, 'Role:', styleRole);
          scene.spritePool.text_role.player1 = scene.add.text(580, 100, 'N/A', styleRole);

          scene.spritePool.image_player.player2Role = scene.add.sprite(1000, 20, `${player2_callsign}Player`).setOrigin(0, 0);
          scene.add.text(1050, 20, trialData.player2.participantid, styleRole);
          scene.add.text(1000, 60, 'Name:', styleRole);
          scene.add.text(1080, 60, trialData.player2.playername, styleRole);

          scene.add.text(1000, 100, 'Role:', styleRole);
          scene.spritePool.text_role.player2 = scene.add.text(1080, 100, 'N/A', styleRole);

          scene.spritePool.image_player.player3Role = scene.add.sprite(1500, 20, `${player3_callsign}Player`).setOrigin(0, 0);
          scene.add.text(1550, 20, trialData.player3.participantid, styleRole);
          scene.add.text(1500, 60, 'Name:', styleRole);
          scene.add.text(1580, 60, trialData.player3.playername, styleRole);

          scene.add.text(1500, 100, 'Role:', styleRole);
          scene.spritePool.text_role.player3 = scene.add.text(1580, 100, 'N/A', styleRole);
        }
      }
      // todo: otherwise, we don't have callsign yet, assign color randomly in the playerMovementColor function
    });

    //------- UPDATE MAP, triggered by testbed messages
    // (** GROUTH TRUTH & YELLOW VICTIMS EXPIRED **)
    // 1. Add new victims (both green and yellow) to the map
    Map.socketFrontEnd.on(Map.socketKey.groundtruth_victim_list, function (data) {
      data.forEach((victim) => {
        scale_x = scaleXCoordinate(victim.x, scene);
        scale_z = scaleZCoordinate(victim.z, scene);
        const key = `${victim.unique_id}` || `${scale_x},${scale_z}`;

        if (victim.block_type === 'block_victim_1' || victim.block_type === 'block_victim_1b') {
          // green
          Map.victims.regular += 1;
          const regular_victim_sprite = scene.add.sprite(scale_x, scale_z, 'green_victim').setOrigin(0, 0);
          scene.spritePool.image_green_victim[key] = regular_victim_sprite;
        } else if (victim.block_type === 'block_victim_2' || victim.block_type === 'block_victim_proximity') {
          // yellow
          Map.victims.critical += 1;
          const critical_victim_sprite = scene.add.sprite(scale_x, scale_z, 'yellow_victim').setOrigin(0, 0);
          scene.spritePool.image_yellow_victim[key] = critical_victim_sprite;
        }
      });
    });

    // 2. Add holes & additional dynamic blockages to the map
    Map.socketFrontEnd.on(Map.socketKey.groundtruth_blockage_list, function (data) {
      data.forEach((blockage) => {
        let blockage_sprite = null;
        scale_x = scaleXCoordinate(blockage.x, scene);
        scale_z = scaleZCoordinate(blockage.z, scene);
        if (blockage.y === selected_floor) {
          if (blockage.block_type === 'air' && blockage.feature_type === 'Opening - Passable') {
            blockage_sprite = scene.add.sprite(scale_x, scale_z, 'passable').setOrigin(0, 0);
          } else if (blockage.block_type === 'bedrock') {
            blockage_sprite = scene.add.sprite(scale_x, scale_z, 'bedrock').setOrigin(0, 0);
          } else if (blockage.block_type === 'gravel') {
            blockage_sprite = scene.add.sprite(scale_x, scale_z, 'gravel').setOrigin(0, 0);
          }
          scene.spritePool.image_blockage[`${scale_x},${scale_z}`] = blockage_sprite;
        }
      });
    });

    // 3. Add Freeze blockages
    Map.socketFrontEnd.on(Map.socketKey.groundtruth_freezeBlock_list, function (data) {
      addBlockage(data, scene, 'block_freeze_player', 'frozen');
    });

    // 4. Add Threat Sign
    Map.socketFrontEnd.on(Map.socketKey.groundtruth_threatsign_list, function (data) {
      addBlockage(data, scene, 'redstone_block', 'redstone');
    });

    // 3. Yellow victims timeout (only in Falcon)
    Map.socketFrontEnd.on(Map.socketKey.groundtruth_victims_expired, function (data) {
      const sprites = scene.spritePool.image_yellow_victim;
      let coordinates = null;
      printMessage(isTesting, `Info: yellow victims timeout ${data}`);
      for (const key in sprites) {
        coordinates = key.split(',');
        // Remove the current victim sprite
        sprites[key].destroy(); // remove the sprite from the scen
        delete sprites[key]; // Delete from the dictionary

        // Add the dead victim sprites
        const dead_victim_sprite = scene.add.sprite(coordinates[0], coordinates[1], 'red_victim').setOrigin(0, 0);
        scene.spritePool.image_dead_victim[`${coordinates[0]},${coordinates[1]}`] = dead_victim_sprite;
      }
    });

    // (** DOOR, TRIAGE, and PLAYER'S MOVEMENT EVENTS **)
    // 1. Door (open/close)
    Map.socketFrontEnd.on(Map.socketKey.event_door, function (state_and_coordinates) {
      const { coordinates, participantId, state } = state_and_coordinates;
      const { z, x } = coordinates;
      scale_x = scaleXCoordinate(x, scene);
      scale_z = scaleZCoordinate(z, scene);
      const key_string = `${scale_x},${scale_z}`;

      if (state) {
        const door_sprite = scene.add.sprite(scale_x, scale_z, 'open_door').setScale(0.5).setOrigin(0, 0); // setScale(0.5 b/c 32x32 pixels
        scene.spritePool.image_door_open[key_string] = door_sprite;
        printMessage(isTesting, `Event Door: ${participantId} opens door`);
      } else {
        const sprites = scene.spritePool.image_door_open;
        if (sprites[key_string]) {
          sprites[key_string].destroy();
          delete sprites[key_string];
        }
        printMessage(isTesting, `Event Door: ${participantId} closes door`);
      }
    });

    // 2. Triage (IN_PROGRESS/ SUCCESSFUL/ UNSUCCESSFUL)
    Map.socketFrontEnd.on(Map.socketKey.event_triage, function (state_and_coordinates) {
      const { victimId, victimLocation, victimType, participantId } = state_and_coordinates;
      const key = `${victimId}` || `${scale_x},${scale_z}`;
      const { z, x } = victimLocation;
      scale_x = scaleXCoordinate(x, scene);
      scale_z = scaleZCoordinate(z, scene);

      if (victimType.toLowerCase() === 'regular') {
        printMessage(isTesting, `Event Triage: ${participantId} successfully triaged a ${victimType} victim, id ${key}`);
        const triaged_green = scene.add.sprite(scale_x, scale_z, 'green_victim_triaged').setOrigin(0, 0);
        scene.spritePool.image_triaged_green_victim[key] = triaged_green;
        scene.spritePool.image_green_victim[key].destroy();
        delete scene.spritePool.image_green_victim[key];
      } else {
        printMessage(isTesting, `Event Triage: ${participantId} successfully triaged a ${victimType} victim, id ${key}`);
        const triaged_yellow = scene.add.sprite(scale_x, scale_z, 'yellow_victim_triaged').setOrigin(0, 0);
        scene.spritePool.image_triaged_yellow_victim[key] = triaged_yellow;
        scene.spritePool.image_yellow_victim[key].destroy();
        delete scene.spritePool.image_yellow_victim[key];
      }
    });

    // 3. Victim placed (EVENT)
    Map.socketFrontEnd.on(Map.socketKey.event_victimPlaced, function (data) {
      const { participantId, victimId, victimType, victimLocation } = data;
      // x202202231 { x: -2161, z: 52 } regular_triaged 32
      // participantId, victimLocation, victimType, victimId

      const { z, x } = victimLocation;
      scale_x = scaleXCoordinate(x, scene);
      scale_z = scaleZCoordinate(z, scene);
      const key = `${victimId}` || `${scale_x},${scale_z}`;

      if (victimType.toLowerCase() === 'regular') {
        const regular_victim_sprite = scene.add.sprite(scale_x, scale_z, 'green_victim').setOrigin(0, 0);
        scene.spritePool.image_green_victim[key] = regular_victim_sprite;
      } else if (victimType.toLowerCase() === 'regular_triaged') {
        const regular_victim_sprite = scene.add.sprite(scale_x, scale_z, 'green_victim_triaged').setOrigin(0, 0);
        scene.spritePool.image_triaged_green_victim[key] = regular_victim_sprite;
      } else if (victimType.toLowerCase() === 'critical_triaged') {
        const critical_victim_sprite = scene.add.sprite(scale_x, scale_z, 'yellow_victim_triaged').setOrigin(0, 0);
        scene.spritePool.image_triaged_yellow_victim[key] = critical_victim_sprite;
      } else {
        const critical_victim_sprite = scene.add.sprite(scale_x, scale_z, 'yellow_victim').setOrigin(0, 0);
        scene.spritePool.image_yellow_victim[key] = critical_victim_sprite;
      }
      printMessage(isTesting, `Event: ${participantId} placed a ${victimType} - victim, id: ${key}`);
    });

    // Victim picked up (Event)
    Map.socketFrontEnd.on(Map.socketKey.event_victimPickedUp, function (data) {
      const { participantId, victimId, victimType, victimLocation } = data;
      const { z, x } = victimLocation;
      scale_x = scaleXCoordinate(x, scene);
      scale_z = scaleZCoordinate(z, scene);
      const key = `${victimId}` || `${scale_x},${scale_z}`;

      if (victimType.toLowerCase() === 'regular') {
        scene.spritePool.image_green_victim[key].destroy();
        delete scene.spritePool.image_green_victim[key];
      } else if (victimType.toLowerCase() === 'regular_triaged') {
        scene.spritePool.image_triaged_green_victim[key].destroy();
        delete scene.spritePool.image_triaged_green_victim[key];
      } else if (victimType.toLowerCase() === 'critical_triaged') {
        scene.spritePool.image_triaged_yellow_victim[key].destroy();
        delete scene.spritePool.image_triaged_yellow_victim[key];
      } else {
        scene.spritePool.image_yellow_victim[key].destroy();
        delete scene.spritePool.image_yellow_victim[key];
      }
      printMessage(isTesting, `Event: ${participantId} is carrying a ${victimType} - victim, id: ${key}`);
    });

    // 4. Rubble Collapsed
    Map.socketFrontEnd.on(Map.socketKey.event_rubbleCollapse, function (data) {
      for (const item of data) {
        const { x, z, y } = item;
        scale_x = scaleXCoordinate(x, scene);
        scale_z = scaleZCoordinate(z, scene);
        printMessage(isTesting, `Event Rubble: Rubble collapsed at ${x}, ${z}, ${y}`);

        // If we already have a block, don't duplicate it
        if (!scene.spritePool.image_blockage[`${scale_x},${scale_z}`]) {
          const blockage_sprite = scene.add.sprite(scale_x, scale_z, 'bedrock').setOrigin(0, 0);
          scene.spritePool.image_blockage[`${scale_x},${scale_z}`] = blockage_sprite;
        }
      }
    });

    // 4. Rubble Destroyed
    Map.socketFrontEnd.on(Map.socketKey.event_rubbleDestroyed, function (data) {
      const { participantId, rubbleLocation } = data;
      const { z, x, y } = rubbleLocation;
      scale_x = scaleXCoordinate(x, scene);
      scale_z = scaleZCoordinate(z, scene);
      if (y === selected_floor && scene.spritePool.image_blockage[`${scale_x},${scale_z}`]) {
        printMessage(isTesting, `Event Rubble: ${participantId} destroyed a rubble ${scale_x}, ${scale_z}`);
        scene.spritePool.image_blockage[`${scale_x},${scale_z}`].destroy();
        delete scene.spritePool.image_blockage[`${scale_x},${scale_z}`];
      }
    });

    // 5. Frozen
    Map.socketFrontEnd.on(Map.socketKey.event_frozen, function (data) {
      const { participantId, isFrozen, medicPlayerName, playerLocation } = data;
      const { x, z } = playerLocation;
      scale_x = scaleXCoordinate(x, scene);
      scale_z = scaleZCoordinate(z, scene);
      const key = `${scale_x},${scale_z}`;

      if (isFrozen) {
        printMessage(isTesting, `Event Frozen: ${participantId} is frozen at (x: ${x}, z: ${z}).`);
        const frozen_sprite = scene.add.sprite(scale_x, scale_z, 'frozenPlayer').setOrigin(0, 0);
        scene.spritePool.image_frozen[key] = frozen_sprite;
      } else {
        printMessage(isTesting, `Event Frozen: ${participantId} is unfrozen by ${medicPlayerName}, at (x: ${x}, z: ${z})`);
        scene.spritePool.image_frozen[key].destroy();
        delete scene.spritePool.image_frozen[key];
      }
    });

    // 6. Player places/removes marker blocks
    Map.socketFrontEnd.on(Map.socketKey.event_markerPlaced, function (data) {
      const { participantId, markerType, markerLocation } = data;
      const { x, z } = markerLocation;
      scale_x = scaleXCoordinate(x, scene);
      scale_z = scaleZCoordinate(z, scene);
      const key = `${scale_x},${scale_z}`;
      printMessage(isTesting, `Event Marker Placed: ${participantId} placed a ${markerType} at location (x: ${x}, z: ${z}).`);
      const marker_sprite = scene.add.sprite(scale_x, scale_z, `marker_${markerType}`).setOrigin(0, 0);
      scene.spritePool.image_marker[key] = marker_sprite;
    });

    Map.socketFrontEnd.on(Map.socketKey.event_markerRemoved, function (data) {
      const { participantId, markerType, markerLocation } = data;
      const { x, z } = markerLocation;
      scale_x = scaleXCoordinate(x, scene);
      scale_z = scaleZCoordinate(z, scene);
      const key = `${scale_x},${scale_z}`;
      printMessage(isTesting, `Event Marker Removed: ${participantId} removed a ${markerType} at location (x: ${x}, z: ${z}).`);
      // OLD data: const marker_sprite = scene.add.sprite(scale_x, scale_z, `marker_${markerColor.toLowerCase()}${markerType}1`).setOrigin(0, 0).setScale(0.5);
      scene.spritePool.image_marker[key].destroy();
      delete scene.spritePool.image_marker[key];
    });

    // 7. Player changes role: add/update text
    Map.socketFrontEnd.on(Map.socketKey.event_roleChange, function (data) {
      const { playerNumber, participantId, new_role, prev_role } = data;
      printMessage(isTesting, `${participantId} changes role from ${prev_role} to ${new_role}`);
      // Changing roles' text
      if (!isFixedRoles) {
        if (playerNumber === 'player1') {
          if (scene.spritePool.text_role.player1 !== null) {
            scene.spritePool.text_role.player1.destroy();
          }
          scene.spritePool.text_role.player1 = scene.add.text(580, 100, new_role, styleRole);
        } else if (playerNumber === 'player2') {
          if (scene.spritePool.text_role.player2 !== null) {
            scene.spritePool.text_role.player2.destroy();
          }
          scene.spritePool.text_role.player2 = scene.add.text(1080, 100, new_role, styleRole);
        } else if (playerNumber === 'player3') {
          if (scene.spritePool.text_role.player3 !== null) {
            scene.spritePool.text_role.player3.destroy();
          }
          scene.spritePool.text_role.player3 = scene.add.text(1580, 100, new_role, styleRole);
        }
      }
    });

    // 8. Player's movements
    // isMultiPlayer, player_pos: player.positions, playerInfo: player
    Map.socketFrontEnd.on(Map.socketKey.observation_player, function (data) {
      const { player_pos, playerInfo } = data;
      playerMovementColor(playerInfo.callSign, player_pos, 'player', 500, scene, isFixedRoles);
    });

    Map.socketFrontEnd.on(Map.socketKey.observation_player1, function (data) {
      const { player_pos, playerInfo } = data;
      playerMovementColor(playerInfo.callSign, player_pos, 'player1', 500, scene, isFixedRoles);
    });

    Map.socketFrontEnd.on(Map.socketKey.observation_player2, function (data) {
      const { player_pos, playerInfo } = data;
      playerMovementColor(playerInfo.callSign, player_pos, 'player2', 1000, scene, isFixedRoles);
    });

    Map.socketFrontEnd.on(Map.socketKey.observation_player3, function (data) {
      const { player_pos, playerInfo } = data;
      playerMovementColor(playerInfo.callSign, player_pos, 'player3', 1500, scene, isFixedRoles);
    });

    // ROBOT MOVEMENT
    Map.socketFrontEnd.on(Map.socketKey.rl_robot_pos, function (data) {
      const x = scaleXCoordinate(data.x, scene);
      const z = scaleZCoordinate(data.z, scene);

      if (scene.graphics_robot === null) {
        scene.graphics_robot = scene.add.graphics();
        scene.graphics_robot.lineStyle(1.3, 0x3aa5ed, 0.8);
      } else if (scene.connector_robot.x !== 0 && scene.connector_robot.z !== 0) {
        scene.graphics_robot.lineBetween(scene.connector_robot.x, scene.connector_robot.z, x, z);
      }
      // Store the "last location of player"
      scene.connector_robot.x = x;
      scene.connector_robot.z = z;

      if (scene.spritePool.image_robot === null) {
        scene.spritePool.image_robot = scene.add.sprite(x, z, 'player3').setOrigin(0, 0); // setScale(0.5 b/c 32x32 pixels)
      } else {
        scene.spritePool.image_robot.setPosition(x, z);
      }
    });

    // Draw next-room-visted predictions
    Map.socketFrontEnd.on(Map.socketKey.prediction_next_room, function (prediction_and_time) {
      let room = prediction_and_time[0].object.split('.').slice(-1)[0];
      // console.log(`ROOM: ${room}`);

      // Add graphics (to draw line) object to the scene, if not already
      if (scene.graphics_room_prediction === null) {
        scene.graphics_room_prediction = scene.add.graphics();
      }
      scene.graphics_room_prediction.lineStyle(2.5, 0x9ab2fc, 1);
      // scene.graphics.fillStyle(0xfae77a, 0.5);

      if (Map.mappingName_SE_UI[`${room}`]) {
        room = Map.mappingName_SE_UI[`${room}`];

        // Add to scene if unknown
        if (prediction_and_time[0].state === 'unknown') {
          let polygon = null;
          if (room === 'FalconKO') {
            // {x: -2044, x1: -2028, y: 143,  x2: -2028, x3: -2037, y2: 159, x4: -2037, x5: -2044, y3: 151,},
            const x = scaleXCoordinate(Map.room_coordinates[`${room}`].x, scene); // x position
            const x1 = scaleXCoordinate(Map.room_coordinates[`${room}`].x1, scene); // x position
            const y = scaleZCoordinate(Map.room_coordinates[`${room}`].y, scene); // z position
            const x2 = scaleXCoordinate(Map.room_coordinates[`${room}`].x2, scene); // x position
            const x3 = scaleXCoordinate(Map.room_coordinates[`${room}`].x3, scene); // x position
            const y2 = scaleZCoordinate(Map.room_coordinates[`${room}`].y2, scene); // z position
            const x4 = scaleXCoordinate(Map.room_coordinates[`${room}`].x4, scene); // x position
            const x5 = scaleXCoordinate(Map.room_coordinates[`${room}`].x5, scene); // x position
            const y3 = scaleZCoordinate(Map.room_coordinates[`${room}`].y3, scene); // z position
            polygon = new Phaser.Geom.Polygon([x, y, x1, y, x2, y2, x3, y2, x4, y3, x5, y3]);
            scene.spritePool.polygon_room[room] = scene.graphics_room_prediction.strokePoints(polygon.points, true);
          } else {
            const left = scaleXCoordinate(Map.room_coordinates[`${room}`].l - 0.2, scene); // x position
            const top = scaleZCoordinate(Map.room_coordinates[`${room}`].t - 0.2, scene); // z position
            const right = scaleXCoordinate(Map.room_coordinates[`${room}`].r + 0.2, scene); // x position
            const bottom = scaleZCoordinate(Map.room_coordinates[`${room}`].b + 0.2, scene); // z position
            polygon = new Phaser.Geom.Polygon([left, top, right, top, right, bottom, left, bottom]);
            scene.spritePool.polygon_room[room] = scene.graphics_room_prediction.strokePoints(polygon.points, true);
          }

          // Remove when that prediction turns true/false
        } else if (scene.spritePool.polygon_room[room]) {
          scene.spritePool.polygon_room[room].destroy();
          delete scene.spritePool.polygon_room[room];
          scene.graphics_room_prediction = null;

          // Room doesn't exist
        } else {
          printMessage(isTesting, `${room} Room doesn't exist`);
        }
      }
    });

    Map.socketFrontEnd.on('mission_stop', function () {
      console.log('Stop listening...');
      // Map.socketFrontEnd.off();
    });
  }

  /*----- 3. Update objects' states as the game progresses ------*/
  // We don't need this step because we don't actually play the game
  // All updates, triggered by rmq events, will be updated in the create() function
  update() {}
}

module.exports = Game;
