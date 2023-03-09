/* eslint-disable camelcase */
/* eslint-disable vars-on-top */
/* eslint-disable no-var */
/* eslint-disable no-undef */
/* eslint-disable no-plusplus */

// Make connection
const socketFrontEnd = io.connect('http://localhost:3000/state-estimation');

/* ----------------------------*/
/* Efficient Level Graph (EL)  */
/* ----------------------------*/
// Initialize variables
const elGraph = document.getElementById('el-graph');
let predictionID = [];
let percentTrue = 0;
let percentFalse = 0;

if (performance.navigation.type === performance.navigation.TYPE_RELOAD) {
  socketFrontEnd.emit('clearState', 'Please clear the current states');
  predictionID = [];
  percentTrue = 0;
  percentFalse = 0;
  console.info('This page is reloaded');
} else {
  console.info('This page is not reloaded');
}

// Initialize graph
var dataEL = [
  {
    y: [0],
  },
];

var layoutEL = {
  hovermode: 'closest',
  title: {
    text: "Prediction's Accuracy Level",
    font: {
      family: 'Courier New, monospace',
      size: 35,
      color: '#ffff',
    },
  },
  xaxis: {
    linecolor: '#228DFF',
    color: '#ffe6ffd2',
    tickformat: ',d',
    title: {
      text: 'Number of predictions',
      font: {
        family: 'monospace',
        size: 18,
        color: '#f9e3ff',
      },
      tickcolor: '#228DFF',
    },
    rangemode: 'nonnegative',
    showspikes: true,
    showgrid: false,
  },
  yaxis: {
    linecolor: '#228DFF',
    color: '#ffe6ffd2',
    tickformat: '%',
    title: {
      text: 'Accuracy',
      font: {
        family: 'monospace',
        size: 18,
        color: '#f9e3ff',
      },
    },
    range: [0, 1],
    rangemode: 'nonnegative',
    showspikes: true,
    showgrid: false,
  },

  paper_bgcolor: 'rgb(0,0,0,0)',
  plot_bgcolor: 'rgb(0,0,0,0)',
};

var configEL = {
  scrollZoom: true,
  responsive: true,
};

Plotly.plot(elGraph, dataEL, layoutEL, configEL);

// Update graph as data coming in
socketFrontEnd.on('newPrediction', function (data) {
  // If new prediction, append to the "monitor"
  if (!predictionID.includes(data.uid)) {
    predictionID.push(data.uid);
    // console.log(data.uid, predictionID.length);
    $('.prediction ul').prepend(`<li class="predictionWrapper"> ${data.prediction} - ${data.uid}</li>`);
  }

  // Status = true
  if (data.state === true || data.state === 'true') {
    percentTrue = data.accuracy.toFixed(2) * 100;
    percentFalse = 100 - percentTrue;

    // Section 2: change color and fadeout, then remove
    if ($(`.prediction ul li:contains(${data.uid})`).length > 0) {
      $(`.prediction ul li:contains(${data.uid})`).addClass('fadeOutTrue');
      setTimeout(function () {
        // wait a bit before removing (animation)
        $(`.prediction ul li:contains(${data.uid})`).remove();
      }, 3000);
    }

    // Mini boxes (Section 3)
    $('.true ul').prepend(`<li class="miniBoxID"> ${data.uid}`);
    $(`.true h3`).text(`True: ${percentTrue}%`);
    $(`.false h3`).text(`False: ${percentFalse}%`);

    // Update Graph (Section 3)
    Plotly.extendTraces(elGraph, { y: [[data.accuracy]] }, [0]);

    // Status = false
  } else if (data.state === false || data.state === 'false') {
    // Section 2: change color and fadeout, then remove
    if ($(`.prediction ul li:contains(${data.uid})`).length > 0) {
      $(`.prediction ul li:contains(${data.uid})`).addClass('fadeOutFalse');
      setTimeout(function () {
        // wait a bit before removing (animation)
        $(`.prediction ul li:contains(${data.uid})`).remove();
      }, 3000);
    }

    // Section 3 (Mini boxes)
    $('.false ul').prepend(`<li class="miniBoxID"> ${data.uid}`);

    // Update Graph (Section 3)
    Plotly.extendTraces(elGraph, { y: [[data.accuracy]] }, [0]);
  }
});

/* -------------------------------------------------------*/
/* Cognitive Load graph (CL) && Memory Count Graph (MC) */
/* ------------------------------------------------------*/
var clGraph = document.getElementById('cl-graph');
var mcGraph = document.getElementById('mc-graph');

// a. Initialize graph: Cognitive Level Graph (CL)
var dataCL = [
  { x: [0], y: [0], stackgroup: 'one', fillcolor: '#457b9d', name: 'Engineer' }, // blue/engineer
  { x: [0], y: [0], stackgroup: 'one', fillcolor: '#881d1d', name: 'Medic' }, // red/medic
  { x: [0], y: [0], stackgroup: 'one', fillcolor: '#4b8a4a', name: 'Transporter' }, // green/evacuator
];

var layoutCL = {
  margin: { t: 150, l: 100, r: 100, b: 100, pad: 20 },
  title: {
    text: 'Cognitive Load',
    font: {
      family: 'Bellota, monospace',
      size: 30,
      color: '#ffff',
    },
  },
  legend: {
    font: {
      family: 'monospace',
      size: 14,
      color: '#bebebe',
    },
    bgcolor: '#151515',
  },
  xaxis: {
    autorange: true,
    linecolor: '#bebebe',
    color: '#bebebe',
    tickformat: ',d',
    title: {
      text: 'Time in Minutes',
      font: {
        family: 'monospace',
        size: 18,
        color: '#bebebe',
      },
    },
    rangemode: 'nonnegative',
    showspikes: true,
    showgrid: false,
  },
  yaxis: {
    autorange: true,
    linecolor: '#bebebe',
    color: '#bebebe',
    title: {
      text: 'Bits',
      font: {
        family: 'monospace',
        size: 18,
        color: '#bebebe',
      },
    },
    range: [0, 150],
    rangemode: 'nonnegative',
    showspikes: true,
    showgrid: false,
  },

  paper_bgcolor: 'rgb(0,0,0,0)',
  plot_bgcolor: 'rgb(0,0,0,0)',
};

var configCL = {
  scrollZoom: true,
  responsive: true,
};

Plotly.plot(clGraph, dataCL, layoutCL, configCL);

// a-2. Update graphes based on incoming data
socketFrontEnd.on('timestampMin', function (data) {
  setTimeout(() => {
    Plotly.extendTraces(
      clGraph,
      {
        x: [[data.timeInMin], [data.timeInMin], [data.timeInMin]],
        y: [[data.cognitiveLoad.cognitiveLoadEngineer], [data.cognitiveLoad.cognitiveLoadMedic], [data.cognitiveLoad.cognitiveLoadTransporter]],
      },
      [0, 1, 2]
    );
  }, 300);
});

// b-1. Initialize graph: Memory Count Graph (MC)
var xValue = ['Room Skipped', 'Critical Victim Skipped', 'Normal Victim Skipped', 'Critical Marker Skipped', 'Normal Marker Skipped'];

var dataMC = [
  // Pink Neon color: ff70ae
  {
    x: xValue,
    y: [0, 0, 0, 0, 0],
    name: 'Engineer',
    type: 'bar',
    marker: {
      color: '#457b9d',
      line: {
        width: 1.5,
      },
    },
  },
  {
    x: xValue,
    y: [0, 0, 0, 0, 0],
    name: 'Medic',
    type: 'bar',
    marker: {
      color: '#881d1d',
      line: {
        width: 1.5,
      },
    },
  },
  {
    x: xValue,
    y: [0, 0, 0, 0, 0],
    name: 'Transporter',
    type: 'bar',
    marker: {
      color: '#4b8a4a',
      line: {
        width: 1.5,
      },
    },
  },
];

var layoutMC = {
  barmode: 'stack', // or group
  margin: {
    t: 150,
    l: 100,
    r: 100,
    b: 150,
  },
  title: {
    text: 'Memory Pieces',
    font: {
      family: 'Bellota, monospace',
      size: 30,
      color: '#ffff',
    },
  },
  legend: {
    font: {
      family: 'monospace',
      size: 14,
      color: '#bebebe',
    },
    bgcolor: '#151515',
  },
  xaxis: {
    linecolor: '#bebebe',
    tickfont: {
      size: 15,
      color: '#bebebe',
      family: 'monospace',
    },
  },
  yaxis: {
    autorange: true,
    linecolor: '#bebebe',
    color: '#bebebe',
    title: {
      text: 'Bits',
      font: {
        family: 'monospace',
        size: 18,
        color: '#bebebe',
      },
    },
    showspikes: true,
    showgrid: false,
    rangemode: 'nonnegative',
  },

  paper_bgcolor: 'rgb(0,0,0,0)',
  plot_bgcolor: 'rgb(0,0,0,0)',
};

var configMC = {
  scrollZoom: true,
  responsive: true,
};

Plotly.plot(mcGraph, dataMC, layoutMC, configMC);

// b-2. Update Memory Count Graph (MC)
socketFrontEnd.on('newCognitiveLoad', function (data) {
  let indice = 0;
  if (data.callSign.toLowerCase() === 'red') indice = 1;
  if (data.callSign.toLowerCase() === 'green') indice = 2;

  setTimeout(function () {
    const updatedValue = [data.skippedRooms, data.skippedCVs, data.skippedNVs, data.skippedCMs, data.skippedNMs];
    Plotly.restyle(mcGraph, 'y', [updatedValue], [indice]);
  }, 300);
});

/* -------------------------------------------------------*/
/* Stress Level GRaph (SL) */
/* ------------------------------------------------------*/
// var slGraph = document.getElementById('sl-graph');

// Initialize graph: Stress Level Graph (CL)
// var cardiac_signal = {
//   x: [0],
//   y: [0],
//   mode: 'lines',
//   name: 'cardiac_signal',
//   line: {
//     color: '#e36464',
//     width: 0.9,
//   },
// };

// var oxygenated_blood_signal1 = {
//   x: [0],
//   y: [0],
//   mode: 'lines',
//   name: 'oxygenated_signal1',
//   line: {
//     color: '#5fad82',
//     width: 0.9,
//   },
// };

// var oxygenated_blood_signal2 = {
//   x: [0],
//   y: [],
//   mode: 'lines',
//   name: 'oxygenated_signal2',
// };

// var deoxygenated_signal1 = {
//   x: [0],
//   y: [0],
//   mode: 'lines',
//   name: 'deoxygenated_signal1',
//   line: {
//     color: '#e0dd7b',
//     width: 0.9,
//   },
// };

// var deoxygenated_signal2 = {
//   x: [0],
//   y: [0],
//   mode: 'lines',
//   name: 'deoxygenated_signal2',
// };

// var total_oxygenated_signal1 = {
//   x: [0],
//   y: [0],
//   mode: 'lines',
//   name: 'total_oxygenated_signal1',
//   line: {
//     color: 'rgba(99, 99, 191, 0.6)',
//     width: 0.9,
//   },
// };

// var total_oxygenated_signal2 = {
//   x: [0],
//   y: [0],
//   mode: 'lines',
//   name: 'total_oxygenated_signal2',
// };

// var dataSL = [
//   cardiac_signal,
//   oxygenated_blood_signal1,
//   // oxygenated_blood_signal2,
//   deoxygenated_signal1,
//   // deoxygenated_signal2,
//   total_oxygenated_signal1,
//   // total_oxygenated_signal2,
// ];

// var layoutSL = {
//   hovermode: 'closest',
//   title: {
//     text: 'fNIRs',
//     font: {
//       family: 'Courier New, monospace',
//       size: 30,
//       color: '#ffff',
//     },
//   },
//   xaxis: {
//     linecolor: '#228DFF',
//     color: '#ffe6ffd2',

//     spikemode: 'across',
//     showspikes: true,
//     spikedash: 'solid',
//     spikethickness: 0.5,

//     showgrid: false,
//     range: [0, 10],
//     title: {
//       text: 'Minutes',
//       font: {
//         family: 'monospace',
//         size: 18,
//         color: '#f9e3ff',
//       },
//       tickcolor: '#228DFF',
//     },
//     rangemode: 'nonnegative',
//   },

//   yaxis: {
//     autorange: true,
//     linecolor: '#228DFF',
//     color: '#ffe6ffd2',
//     title: {
//       text: '',
//       font: {
//         family: 'monospace',
//         size: 18,
//         color: '#f9e3ff',
//       },
//     },
//     showspikes: true,
//     spikethickness: 0.5,
//     showgrid: false,
//   },

//   shapes: [
//     //line vertical
//     {
//       type: 'line',
//       x0: 0,
//       y0: 4,
//       x1: 0,
//       y1: -8,
//       line: {
//         color: 'rgba(27, 110, 218, 0.64)',
//         width: 0.8,
//       },
//     },
//   ],

//   paper_bgcolor: 'rgb(0,0,0,0)',
//   plot_bgcolor: 'rgb(0,0,0,0)',
// };

// var configSL = {
//   scrollZoom: true,
//   responsive: true,
// };

// Plotly.newPlot(slGraph, dataSL, layoutSL, configSL);

// socketFrontEnd.on('newFNIRS', function (data) {
//   setTimeout(function () {
//     var updatedData = {
//       x: [data.timeInMin],
//       y: [
//         data.cardiac_signal,
//         data.oxygenated_blood_signal1,
//         // data.oxygenated_blood_signal2,
//         data.deoxygenated_signal1,
//         // data.deoxygenated_signal2,
//         data.total_oxygenated_signal1,
//         // data.total_oxygenated_signal2,
//       ],
//     };

//     var updateLayout = {
//       'shapes[0].x0': data.currentTime,
//       'shapes[0].x1': data.currentTime,
//     };

//     if (data.stressLevel === 1) {
//       $(`#stress-icon-1`).css('opacity', '0.9');
//       $(`#stress-icon-2`).css('opacity', '0.3');
//       $(`#stress-icon-3`).css('opacity', '0.3');
//     } else if (data.stressLevel === 2) {
//       $(`#stress-icon-1`).css('opacity', '0.3');
//       $(`#stress-icon-2`).css('opacity', '0.9');
//       $(`#stress-icon-3`).css('opacity', '0.3');
//     } else if (data.stressLevel === 3) {
//       $(`#stress-icon-1`).css('opacity', '0.3');
//       $(`#stress-icon-2`).css('opacity', '0.3');
//       $(`#stress-icon-3`).css('opacity', '0.9');
//     }

//     Plotly.update(slGraph, updatedData);
//     Plotly.relayout(slGraph, updateLayout);
//   }, 300);
// });
