/* eslint-disable eqeqeq */
/* eslint-disable radix */
/* eslint-disable no-bitwise */
/* eslint-disable no-loop-func */
/* eslint-disable camelcase */
/* eslint-disable vars-on-top */
/* eslint-disable no-var */
/* eslint-disable no-undef */
/* eslint-disable no-plusplus */

/* -------------------------------------------------------*/
/* Story Understanding Graph */
/* ------------------------------------------------------*/
const suGraph = document.getElementById('su-graph');
const initUIInfo = {
  type: 'bar',
  orientation: 'h',
  width: 0.8,
  hoverinfo: 'text',
  textposition: 'auto',
  insidetextanchor: 'middle',
  textfont: {
    color: 'black',
  },
};

// 1. Initalize Graph
const initData = [
  {
    ...initUIInfo,
    text: [],
    y: [],
    x: [], // Duration of the story
    base: [], // The start time (can be the current time-stamp)
    marker: {
      color: [],
      line: {
        color: 'white',
        width: 2,
      },
    },
  },
];

const layout = {
  title: {
    font: {
      //   family: 'Bellota, monospace',
      size: 20,
      color: '#ffff',
    },
    text: 'Story Understanding',
  },
  yaxis: {
    color: '#ffff',
    type: 'category',
    autorange: 'reversed',
    title: {
      text: 'Stories',
      font: {
        family: 'Montserrat, sans-serif',
        size: 13,
      },
    },
  },
  xaxis: {
    type: 'linear',
    color: '#ffff',
    rangemode: 'nonnegative',
    showspikes: true,
    showgrid: false,
    autorange: true,
    title: {
      text: 'Time in seconds',
      font: {
        family: 'Montserrat, sans-serif',
        size: 13,
      },
    },
  },
  margin: {
    l: 150,
    r: 50,
    b: 100,
    t: 100,
  },
  showlegend: false,
  barmode: 'stack',
  bargap: 0,
  paper_bgcolor: 'rgb(0,0,0,0.5)',
  plot_bgcolor: 'rgb(0,0,0,0.5)',
};

const config = {
  scrollZoom: true,
  responsive: true,
};
Plotly.newPlot(suGraph, initData, layout, config);

// Real-time Update
const testData = [
  // Level 1
  {
    id: 'uniqueId-1',
    changed: false,
    storyName: 'Low Hanging Fruit',
    estimatedDuration: 900, // in second - should be in milliseconds (real data)
    startTime: 0,
    parents: [], // Len(parents) = 0
    players: ['playerId1', 'playerId2', 'player3'],
    events: [
      {
        currentEvent: true,
        name: 'everyone is a medic',
        estimatedDuration: 420,
        players: ['playerId1', 'playerId2', 'player3'],
      },
      {
        currentEvent: false,
        name: 'change roles',
        estimatedDuration: 480,
        players: ['playerId1', 'playerId2'],
      },
    ],
  },
  // // Change event
  // { end: true, id: 'uniqueId', endTime: 'timestamp' },
  {
    id: 'uniqueId-1',
    changed: true,
    storyName: 'Low Hanging Fruit',
    estimatedDuration: 900, // in second - should be in milliseconds (real data)
    startTime: 0,
    parents: [], // Len(parents) = 0
    players: ['playerId1', 'playerId2', 'player3'],
    events: [
      // Example of changing the estimatedDuration and players
      {
        currentEvent: true,
        name: 'everyone is a medic',
        estimatedDuration: 300, // in milliseconds
        players: ['playerId1', 'playerId2', 'player3'],
      },
      {
        currentEvent: false,
        name: 'change roles',
        estimatedDuration: 600, // in milliseconds
        players: ['playerId1', 'playerId3'],
      },
    ],
  },
  // // Level 2
  {
    id: 'uniqueId-2',
    changed: false,
    storyName: 'Freeze',
    estimatedDuration: 33, // in milliseconds
    startTime: 180,
    parents: ['uniqueId-1'], // Stack
    players: ['playerId1', null],
    events: [
      {
        currentEvent: false,
        name: 'frozen on plate',
        estimatedDuration: 0.1, // in milliseconds
        players: ['playerId1'],
      },
      {
        currentEvent: true,
        name: 'call for help',
        estimatedDuration: 5,
        players: ['playerId1'],
      },
      {
        currentEvent: false,
        name: 'colleague responds to call',
        estimatedDuration: 3,
        players: ['playerId2'],
      },
      {
        currentEvent: false,
        name: 'colleague comes to rescue',
        estimatedDuration: 10,
        players: ['playerId2'],
      },
      {
        currentEvent: false,
        name: 'unfrozen',
        estimatedDuration: 15,
        players: ['playerId1'],
      },
    ],
  },
  {
    id: 'uniqueId-3',
    changed: false,
    storyName: 'Change role to medic',
    estimatedDuration: 5, // in milliseconds
    startTime: 188,
    parents: ['uniqueId-1', 'uniqueId-2'], // Stack
    players: ['playerId2'],
    events: [
      {
        currentEvent: false,
        name: 'Get medkit',
        estimatedDuration: 5, // in milliseconds
        players: ['playerId2'],
      },
    ],
  },
];
const calculateArrData = function (obj, initalTimestamp) {
  const yLabel = obj.parents.length === 0 ? 'Main Story (MS)' : `Event Lv${obj.parents.length} (ELV${obj.parents.length})`;
  const ySubLabel = obj.parents.length === 0 ? 'MS-' : `ELV${obj.parents.length}`;
  const yAndText = {};
  yAndText[yLabel] = obj.storyName;

  const duration = [obj.estimatedDuration];
  const startTime = [obj.startTime - initalTimestamp, obj.startTime - initalTimestamp];
  for (let j = 0; j < obj.events.length; j++) {
    const eventText = obj.events[j].currentEvent ? obj.events[j].name.toUpperCase() : obj.events[j].name;
    yAndText[`${ySubLabel}${j + 1}`] = `${eventText} (${obj.events[j].players})`;
    if (j > 0) {
      startTime.push(duration.slice(1).reduce((a, b) => a + b) + startTime[0]);
    }
    duration.push(obj.events[j].estimatedDuration);
  }

  return [yAndText, duration, startTime];
};

function shadeColor(color, percent) {
  let R = parseInt(color.substring(1, 3), 16);
  let G = parseInt(color.substring(3, 5), 16);
  let B = parseInt(color.substring(5, 7), 16);

  R = parseInt((R * (100 + percent)) / 100);
  G = parseInt((G * (100 + percent)) / 100);
  B = parseInt((B * (100 + percent)) / 100);

  R = R < 255 ? R : 255;
  G = G < 255 ? G : 255;
  B = B < 255 ? B : 255;

  const RR = R.toString(16).length == 1 ? `0${R.toString(16)}` : R.toString(16);
  const GG = G.toString(16).length == 1 ? `0${G.toString(16)}` : G.toString(16);
  const BB = B.toString(16).length == 1 ? `0${B.toString(16)}` : B.toString(16);

  return `#${RR}${GG}${BB}`;
}

const tracesIndex = {};
let initalTimestamp = null;
for (let i = 0; i < testData.length; i++) {
  const obj = testData[i];

  setTimeout(function () {
    // 1. Handle new trace
    initalTimestamp = initalTimestamp === null ? obj.startTime : initalTimestamp;

    if (!tracesIndex[obj.id]) {
      // Assign color
      let color;
      if (obj.parents.length === 0) {
        color = '#ff8396'; // Assign a new color
      } else {
        const lastParent = obj.parents[obj.parents.length - 1];
        const lastColor = tracesIndex[lastParent][1];
        console.log(tracesIndex, lastParent, lastColor);
        color = shadeColor(lastColor, 40);
      }

      // Add to the traceIndex
      const idx = Object.keys(tracesIndex).length + 1;
      tracesIndex[obj.id] = [idx, color]; // Start at 1

      // Calculate data to be displayed
      const [yAndText, duration, startTime] = calculateArrData(obj, initalTimestamp);
      const data = {
        ...initUIInfo,
        text: Object.values(yAndText),
        y: Object.keys(yAndText),
        x: duration,
        base: startTime,
        marker: {
          color: color,
        },
      };

      // Draw new trace
      Plotly.addTraces(suGraph, data);

      return;
    }

    // 2. Handle existing traces
    if (obj.changed) {
      const [yAndText, duration, startTime] = calculateArrData(obj, initalTimestamp);
      const data_update = {
        text: [Object.values(yAndText)],
        y: [Object.keys(yAndText)],
        x: [duration],
        base: [startTime],
      };

      // Redraw existing trace
      Plotly.restyle(suGraph, data_update, tracesIndex[obj.id][0]); // Which trace to update, hash-table keeping track of trace #
    }
  }, 1000);
}

// socketFrontEnd.on('storyUnderstanding', function (data) {});

// ----------------- //
// EXAMPLE CODE
// ----------------- //

// 1. **** Updating 2 traces at the same time
// var data_update = {
//   text: [['Player1, Player2', 'Player1', 'Player3'], ['test']],
//   y: [['Pop_up_1', 'Pop_up_2', 'Pop_up_3', 'Pop_up_4', 'Pop_up_5'], ['test']],
//   x: [[1, 1, 3, 1, 2], [5]], // Duration of the story
//   base: [[0, 1, 2, 2, 3], [5]], // The start time
//   marker: [
//     {
//       color: ['#964a3a', '#a85341', '#a85341', '#a85341', '#c77e6e', 'c77e6e'],
//     },
//     {
//       color: ['red'],
//     },
//   ],
// };
// Plotly.restyle(suGraph, data_update, [0, 2]); // Which trace to update, hash-table keeping track of trace #

// 2. **** Bar Plot Data
// var examplePlotlyData = [
//   {
//     type: 'bar',
//     orientation: 'h',
//     width: 0.8,
//     hoverinfo: 'text',
//     textposition: 'auto',
//     insidetextanchor: 'middle',
//     textfont: {
//       color: 'red',
//     },

//     text: ['Low Hanging Fruit: P1, P2, P3', 'Everyone is a medic: P1, P2, P3', 'Change roles: P2, P3'],
//     y: ['Main Story (MS)', 'MS: Events 1', 'MS: Events 2'],
//     x: [15, 7, 8], // Duration of the story
//     base: [0, 0, 7], // The start time (can be the current time-stamp)
//     marker: {
//       color: ['#ff8396', '#ff8396', '#ff8396'],
//       line: {
//         color: 'white',
//         width: 2,
//       },
//     },
//   },
//   {
//     textposition: 'auto',
//     textfont: {
//       color: 'black',
//     },
//     type: 'bar',
//     orientation: 'h',
//     width: 0.8,
//     hoverinfo: 'text',
//     text: [
//       'Freeze: P2, ?',
//       'Frozen on plate: P2',
//       'call for help: P2',
//       'colleague responds to call: P3',
//       'colleague come to rescue: P3',
//       'unfrozen: P2',
//     ],
//     y: ['Event Lv1 (ELV1)', 'ELV1 1', 'ELV1 2', 'ELV1 3', 'ELV1 4', 'ELV1 5'],
//     x: [0.803, 0, 0.083, 0.05, 0.17, 0.25], // Duration of the story
//     base: [3, 3, 3.083, 3.13, 3.303, 3.553], // The start time (can be the current time-stamp)
//     marker: {
//       color: '#ffd0d7', // -- Calculated based on parents
//       line: {
//         color: 'white',
//         width: 2,
//       },
//     },
//   },
// ];
