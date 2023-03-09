/* eslint-disable camelcase */
/* eslint-disable vars-on-top */
/* eslint-disable no-var */
/* eslint-disable no-undef */
/* eslint-disable no-plusplus */

const socketFrontEnd = io.connect('http://localhost:3000/stacked-graphs');

/* ------------------------------ */
/* 👉 Timestamp                   */
/* ------------------------------ */
var TSGraph = document.getElementById('time-graph');

var dataTS = [
  {
    x: [0],
    y: [0.5],
    fill: 'tozeroy',
    fillcolor: '#ab63fa',
  },
];

var layoutTS = {
  title: {
    text: 'Time in minutes',
    font: {
      family: 'Montserrat, sans-serif',
      size: 15,
      color: '#e8e5fa',
    },
  },

  margin: {
    l: 50,
    r: 20,
    b: 20,
    t: 40,
  },

  xaxis: {
    linecolor: '#ffe6ffd2',
    color: '#ffe6ffd2',
    rangemode: 'nonnegative',
    // range: [0, 20],
    showspikes: true,
    showgrid: false,
    autorange: true,
  },
  yaxis: {
    visible: false,
  },

  hovermode: 'closest',
  paper_bgcolor: 'rgb(0,0,0,0)',
  plot_bgcolor: 'rgb(0,0,0,0)',
};

var configTS = {
  scrollZoom: true,
  responsive: true,
};

Plotly.plot(TSGraph, dataTS, layoutTS, configTS);

/* ------------------------------ */
/* 👉 Efficient Level Graph (EL)  */
/* ------------------------------ */
// 1. Initialize variables & graph
var elGraph = document.getElementById('el-graph');

var dataEL = [
  {
    x: [0],
    y: [0],
  },
];

var layoutEL = {
  margin: {
    l: 60,
    r: 20,
    b: 20,
    t: 20,
  },

  xaxis: {
    visible: false,
    rangemode: 'nonnegative',
    showspikes: true,
    showgrid: false,
  },

  yaxis: {
    // linecolor: '#228DFF',
    color: '#ffe6ffd2',
    tickformat: '%',
    title: {
      text: 'Accuracy',
      font: {
        family: 'Montserrat, sans-serif',
        size: 13,
        color: '#e8e5fa',
      },
    },
    range: [0, 1],
    rangemode: 'nonnegative',
    showspikes: true,
    // showgrid: false,
  },

  hovermode: 'closest',
  paper_bgcolor: 'rgb(0,0,0,0)',
  plot_bgcolor: 'rgb(0,0,0,0)',
};

var configEL = {
  scrollZoom: true,
  responsive: true,
};

Plotly.plot(elGraph, dataEL, layoutEL, configEL);

/* ----------------------------- */
/* 👉 Cognitive Level Graph (CL) */
/* ----------------------------- */
// 1. Initialize graph: Cognitive Level Graph (CL)
var clGraph = document.getElementById('cl-graph');

var dataCL = [
  {
    x: [0],
    y: [0],
  },
];

var layoutCL = {
  margin: {
    l: 60,
    r: 20,
    b: 20,
    t: 20,
  },
  xaxis: {
    visible: false,
    rangemode: 'nonnegative',
    showspikes: true,
    showgrid: false,
  },
  yaxis: {
    // linecolor: '#228DFF',
    color: '#ffe6ffd2',
    title: {
      text: 'Bits',
      font: {
        family: 'Montserrat, sans-serif',
        size: 13,
        color: '#e8e5fa',
      },
    },
    hovermode: 'closest',
    range: [0, 150],
    rangemode: 'nonnegative',
    showspikes: true,
    // showgrid: false,
  },

  hovermode: 'closest',
  paper_bgcolor: 'rgb(0,0,0,0)',
  plot_bgcolor: 'rgb(0,0,0,0)',
};

var configCL = {
  scrollZoom: true,
  responsive: true,
};

Plotly.plot(clGraph, dataCL, layoutCL, configCL);

/* ------------------------------ */
/* 👉 UPDATE ALL GRAPHS  */
/* ------------------------------ */

socketFrontEnd.on('timestampMin', function (data) {
  setTimeout(() => {
    Plotly.extendTraces(TSGraph, { x: [[data.timeInMin]], y: [[0.5]] }, [0]);
    Plotly.extendTraces(elGraph, { x: [[data.timeInMin]], y: [[data.accuracy]] }, [0]);
    Plotly.extendTraces(clGraph, { x: [[data.timeInMin]], y: [[data.cognitiveLoad]] }, [0]);
  }, 500);
});

/* ------------------------------ */
/* 👉 Hide/Show Graphs            */
/* ------------------------------ */
$('input.predictionAccuracy').click(function () {
  if ($(this).is(':checked')) {
    $('.el-graph').hide();
  } else if ($(this).is(':not(:checked)')) {
    $('.el-graph').show();
  }
});

$('input.cognitiveLoad').click(function () {
  if ($(this).is(':checked')) {
    $('.cl-graph').hide();
  } else if ($(this).is(':not(:checked)')) {
    $('.cl-graph').show();
  }
});

/* ---------------------------------------------------- */
/* 👉 Clear local state before sending data (reset time) */
/* ---------------------------------------------------- */
$('.clear-state').click(function () {
  socketFrontEnd.emit('clearState', 'Please clear the current state');
  Plotly.deleteTraces(elGraph, 0);
  Plotly.deleteTraces(clGraph, 0);
  Plotly.deleteTraces(TSGraph, 0);

  dataTS = [
    {
      x: [0],
      y: [0.5],
      fill: 'tozeroy',
      fillcolor: '#ab63fa',
    },
  ];

  dataEL = [
    {
      x: [0],
      y: [0],
    },
  ];

  dataCL = [
    {
      x: [0],
      y: [0],
    },
  ];

  Plotly.addTraces(TSGraph, dataTS);
  Plotly.addTraces(elGraph, dataEL);
  Plotly.addTraces(clGraph, dataCL);
});

if (performance.navigation.type === performance.navigation.TYPE_RELOAD) {
  console.info('This page is reloaded');
  socketFrontEnd.emit('clearState', 'Please clear the current state');
  Plotly.deleteTraces(elGraph, 0);
  Plotly.deleteTraces(clGraph, 0);
  Plotly.deleteTraces(TSGraph, 0);

  dataTS = [{ x: [0], y: [0.5], fill: 'tozeroy', fillcolor: '#ab63fa' }];

  dataEL = [{ x: [0], y: [0] }];

  dataCL = [{ x: [0], y: [0] }];

  Plotly.addTraces(TSGraph, dataTS);
  Plotly.addTraces(elGraph, dataEL);
  Plotly.addTraces(clGraph, dataCL);
} else {
  console.info('This page is not reloaded');
}
