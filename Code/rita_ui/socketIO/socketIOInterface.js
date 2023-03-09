/* eslint-disable camelcase */
/* eslint-disable no-var */
var RMQemitter = require('../rabbitMQ/processMessage.js').emit;

// var socket;
var allSESockets; // SE = State Estimation
var allSGSockets; // SG = Stacked Graphs
var allMapSockets; // Map = Map pages

const prediction_info = {
  predictionToString: '',
  totalPredictionID: [], // Total Predictions
  correctPredictionID: [], // Correct Predictions
  falsePredictionID: [], // False Predictions
  unknownPredictionID: [], // Unknown Predictions
  accuracy: 0,
  cognitiveLoad: {
    cognitiveLoadEngineer: 0,
    cognitiveLoadMedic: 0,
    cognitiveLoadTransporter: 0,
  },
};

const clearPredictionStats = (predictionData) => {
  predictionData.predictionToString = '';
  predictionData.totalPredictionID = [];
  predictionData.correctPredictionID = [];
  predictionData.falsePredictionID = [];
  predictionData.unknownPredictionID = [];
  predictionData.accuracy = 0;
  predictionData.cognitiveLoad = {
    cognitiveLoadEngineer: 0,
    cognitiveLoadMedic: 0,
    cognitiveLoadTransporter: 0,
  };
};

/*------------------------------------------*/
/*------ Establish SockIO connection -------*/
/*------------------------------------------*/
module.exports = function (io) {
  io.of('/state-estimation').on('connection', async (currentSocket) => {
    allSESockets = io.of('/state-estimation');

    currentSocket.on('clearState', (data) => {
      clearPredictionStats(prediction_info);
      RMQemitter.emit('clearState', data);
    });
  });

  io.of('/stacked-graphs').on('connection', async (currentSocket) => {
    allSGSockets = io.of('/stacked-graphs');

    currentSocket.on('clearState', (data) => {
      clearPredictionStats(prediction_info);
      RMQemitter.emit('clearState', data);
    });
  });

  io.of('/map').on('connection', async (currentSocket) => {
    allMapSockets = io.of('/map');

    currentSocket.on('clearState', (data) => {
      RMQemitter.emit('clearState', data);
    });
  });
};

/*-------------------------------------------*/
/*------ Send data based on Timestamp -------*/
/*-------------------------------------------*/
// Send timestamp, predictions' accuracy, cognitiveLoad to stackedGraphs
RMQemitter.on('timestampInMin', (data) => {
  if (allSGSockets || allSESockets) {
    setTimeout(() => {
      allSGSockets.emit('timestampInMin', {
        timeInMin: data,
        accuracy: prediction_info.accuracy, // calculated and updated in this page, based on incoming new predictions
        cognitiveLoad: prediction_info.cognitiveLoad,
      });
    }, 500);
  }
});

/*------------------------------------------------*/
/*------ Send data based on New Prediction -------*/
/*------------------------------------------------*/
// Calculate the accuracy
// Send prediction string, uid, accuracy to the stateEstimation page
RMQemitter.on('newPrediction', (data) => {
  // Add the data to predictionID arry to keep track of data
  if (!prediction_info.totalPredictionID.includes(data[0].uid)) {
    prediction_info.totalPredictionID.push(data[0].uid);
  }

  if (data[0].state === true || data[0].state === 'true') {
    prediction_info.correctPredictionID.push(data[0].uid);
    if (prediction_info.unknownPredictionID.indexOf(data[0].uid) !== -1) {
      prediction_info.unknownPredictionID.splice(prediction_info.unknownPredictionID.indexOf(data[0].uid), 1);
    }
    // Remove from Fail Prediction (caused by time lag problems)
    if (prediction_info.falsePredictionID.indexOf(data[0].uid) !== -1) {
      prediction_info.falsePredictionID.splice(prediction_info.falsePredictionID.indexOf(data[0].uid), 1);
    }
  } else if (data[0].state === false || data[0].state === 'false') {
    prediction_info.falsePredictionID.push(data[0].uid);
    // Remove from Unknown Prediction
    if (prediction_info.unknownPredictionID.indexOf(data[0].uid) !== -1) {
      prediction_info.unknownPredictionID.splice(prediction_info.unknownPredictionID.indexOf(data[0].uid), 1);
    }
  } else {
    prediction_info.unknownPredictionID.push(data[0].uid);
  }
  // console.log(`
  //     Failure: ${prediction_info.falsePredictionID.length},
  //     Correct: ${prediction_info.correctPredictionID.length},
  //     Unknown: ${prediction_info.unknownPredictionID.length},
  //     Total: ${prediction_info.totalPredictionID.length},
  //     Acc: ${prediction_info.accuracy}`);

  // Update accuracy
  prediction_info.accuracy =
    prediction_info.correctPredictionID.length / (prediction_info.totalPredictionID.length - prediction_info.unknownPredictionID.length);

  if (allSESockets) {
    setTimeout(() => {
      prediction_info.predictionToString = `${data[0].subject} will ${data[0].action.replace('-', ' ')} ${data[0].object} with a probability of ${
        data[0]['agent-belief']
      }.`;

      allSESockets.emit('newPrediction', {
        prediction: prediction_info.predictionToString, // String
        uid: data[0].uid, // String
        state: data[0].state, // boolean
        accuracy: prediction_info.accuracy,
      });
    }, 500);
  }
});

/*----------------------------------------------------*/
/*------ Send data based on New Cognitive Load -------*/
/*----------------------------------------------------*/
RMQemitter.on('newCognitiveLoad', (data) => {
  if (data[1].toLowerCase() === 'red') {
    prediction_info.cognitiveLoad.cognitiveLoadMedic = data[0]['total-cognitive-load'];
  } else if (data[1].toLowerCase() === 'green') {
    prediction_info.cognitiveLoad.cognitiveLoadTransporter = data[0]['total-cognitive-load'];
  } else if (data[1].toLowerCase() === 'blue') {
    prediction_info.cognitiveLoad.cognitiveLoadEngineer = data[0]['total-cognitive-load'];
  }

  if (allSESockets !== undefined) {
    setTimeout(() => {
      allSESockets.emit('newCognitiveLoad', {
        cognitiveLoad: data[0]['total-cognitive-load'],
        skippedRooms: data[0]['skipped-rooms'],
        skippedCVs: data[0]['skipped-critical-victims'],
        skippedNVs: data[0]['skipped-normal-victims'],
        skippedCMs: data[0]['skipped-critical-markers'],
        skippedNMs: data[0]['skipped-normal-markers'],
        callSign: data[1],
      });
    }, 500);
  }
});

const sendData2DMap = async (fn) => {
  if (allMapSockets) fn();
};
/*----------------------------------------------------*/
/*------ Send data based on SE Predictions -------*/
/*----------------------------------------------------*/
RMQemitter.on('next_room_predictions', (prediction) => {
  sendData2DMap(() => allMapSockets.emit('next_room_predictions', prediction));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('next_room_predictions', prediction);
  //   }, 500);
  // }
});

RMQemitter.on('story_understanding_predictions', (prediction) => {
  sendData2DMap(() => allMapSockets.emit('story_understanding_predictions', prediction[0]));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('story_understanding_predictions', prediction[0]);
  //   }, 500);
  // }
});

RMQemitter.on('final_score_predictions', (prediction) => {
  sendData2DMap(() => allMapSockets.emit('final_score_predictions', prediction[0]));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('final_score_predictions', prediction[0]);
  //   }, 500);
  // }
});

/*----------------------------------------------*/
/*------ Send data (from Testbed) to Map -------*/
/*----------------------------------------------*/
RMQemitter.on('trial_start', (data) => {
  sendData2DMap(() => {
    const mapName = `${data.experiment_mission} ${data.name} ${data.map_name}`;
    allMapSockets.emit('trial_start', { mapName, data });
  });
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     const map_name = `${data.experiment_mission} ${data.name}`;
  //     allMapSockets.emit('trial_start', { map_name, data });
  //   }, 500);
  // }
});

RMQemitter.on('trial_stop', (data) => {
  sendData2DMap(() => allMapSockets.emit('trial_stop', data));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('trial_stop', data);
  //   }, 500);
  // }
});

RMQemitter.on('mission_start', (data) => {
  sendData2DMap(() => allMapSockets.emit('mission_start', data));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('mission_start', data);
  //   }, 500);
  // }
});

RMQemitter.on('mission_stop', (data) => {
  sendData2DMap(() => allMapSockets.emit('mission_stop', data));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('mission_stop', data);
  //   }, 500);
  // }
});

RMQemitter.on('mission_timer', (data) => {
  sendData2DMap(() => allMapSockets.emit('mission_timer', data));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('mission_timer', data);
  //   }, 500);
  // }
});

RMQemitter.on('victim_list', (victimList) => {
  sendData2DMap(() => allMapSockets.emit('victim_list', victimList));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('victim_list', victim_list);
  //   }, 500);
  // }
});

RMQemitter.on('blockage_list', (blockageList) => {
  sendData2DMap(() => allMapSockets.emit('blockage_list', blockageList));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('blockage_list', blockage_list);
  //   }, 500);
  // }
});

RMQemitter.on('freezeBlock_list', (freezeBlockList) => {
  sendData2DMap(() => allMapSockets.emit('freezeBlock_list', freezeBlockList));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('freezeBlock_list', freezeBlock_list);
  //   }, 500);
  // }
});

RMQemitter.on('threatsign_list', (threatsignList) => {
  sendData2DMap(() => allMapSockets.emit('threatsign_list', threatsignList));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('threatsign_list', threatsign_list);
  //   }, 500);
  // }
});

RMQemitter.on('victims_timeout', (data) => {
  sendData2DMap(() => allMapSockets.emit('victims_timeout', data));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('victims_timeout', message);
  //   }, 500);
  // }
});

RMQemitter.on('victims_rescued', (data) => {
  sendData2DMap(() => allMapSockets.emit('victims_rescued', data));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('victims_rescued', message);
  //   }, 500);
  // }
});

RMQemitter.on('event_roleChange', (message) => {
  sendData2DMap(() => allMapSockets.emit('event_roleChange', message));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('event_roleChange', message);
  //   }, 500);
  // }
});

RMQemitter.on('pause', (status) => {
  sendData2DMap(() => allMapSockets.emit('pause', status.pause_status));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('pause', status.pause_status);
  //   }, 500);
  // }
});

RMQemitter.on('event_door', (data) => {
  sendData2DMap(() => allMapSockets.emit('event_door', data));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('event_door', data);
  //   }, 500);
  // }
});

RMQemitter.on('event_triage', (data) => {
  sendData2DMap(() => allMapSockets.emit('event_triage', data));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('event_triage', data);
  //   }, 500);
  // }
});

RMQemitter.on('event_evacuation', (data) => {
  sendData2DMap(() => allMapSockets.emit('event_evacuation', data));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('event_evacuation', data);
  //   }, 500);
  // }
});

RMQemitter.on('event_perturbation', (data) => {
  sendData2DMap(() => allMapSockets.emit('event_perturbation', data));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('event_perturbation', data);
  //   }, 500);
  // }
});

RMQemitter.on('event_victimPlaced', (data) => {
  sendData2DMap(() => allMapSockets.emit('event_victimPlaced', data));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('event_victimPlaced', data);
  //   }, 500);
  // }
});

RMQemitter.on('event_victimPickedUp', (data) => {
  sendData2DMap(() => allMapSockets.emit('event_victimPickedUp', data));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('event_victimPickedUp', data);
  //   }, 500);
  // }
});

RMQemitter.on('event_rubbleDestroyed', (data) => {
  sendData2DMap(() => allMapSockets.emit('event_rubbleDestroyed', data));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('event_rubbleDestroyed', data);
  //   }, 500);
  // }
});

RMQemitter.on('event_rubbleCollapse', (data) => {
  sendData2DMap(() => allMapSockets.emit('event_rubbleCollapse', data));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('event_rubbleCollapse', data);
  //   }, 500);
  // }
});

RMQemitter.on('event_playerFrozen', (data) => {
  sendData2DMap(() => allMapSockets.emit('event_playerFrozen', data));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('event_playerFrozen', data);
  //   }, 500);
  // }
});

RMQemitter.on('event_markerPlaced', (data) => {
  sendData2DMap(() => allMapSockets.emit('event_markerPlaced', data));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('event_markerPlaced', data);
  //   }, 500);
  // }
});

RMQemitter.on('event_markerRemoved', (data) => {
  sendData2DMap(() => allMapSockets.emit('event_markerRemoved', data));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('event_markerRemoved', data);
  //   }, 500);
  // }
});

RMQemitter.on('event_scoreBoard', (score) => {
  sendData2DMap(() => allMapSockets.emit('event_scoreBoard', score));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('event_scoreBoard', score);
  //   }, 500);
  // }
});

RMQemitter.on('player_position', (playerPosition) => {
  // sendData2DMap(() => allMapSockets.emit('player_position', playerPosition));
  if (allMapSockets !== undefined) {
    setTimeout(() => {
      allMapSockets.emit('player_position', playerPosition);
    }, 500);
  }
});

RMQemitter.on('player1_position', (playerPosition) => {
  // sendData2DMap(() => allMapSockets.emit('player1_position', playerPosition));
  if (allMapSockets !== undefined) {
    allMapSockets.emit('player1_position', playerPosition);
  }
});

RMQemitter.on('player2_position', (playerPosition) => {
  if (allMapSockets !== undefined) {
    allMapSockets.emit('player2_position', playerPosition);
  }
});

RMQemitter.on('player3_position', (playerPosition) => {
  if (allMapSockets !== undefined) {
    allMapSockets.emit('player3_position', playerPosition);
  }
});

RMQemitter.on('robot_pos', (data) => {
  sendData2DMap(() => allMapSockets.emit('robot_pos', data));
  // if (allMapSockets !== undefined) {
  //   setTimeout(() => {
  //     allMapSockets.emit('robot_pos', data);
  //   }, 500);
  // }
});
