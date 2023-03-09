const fs = require('fs-extra');
const path = require('path');
const terminate = require('terminate');
const establishConnAndSendMessage = require('../rabbitMQ/rabbitMQ-send.js').establishConnAndSendMessage;

let arrPidObjs = {};
let rmq_speed = 10;

// module.exports.emit = emit;
module.exports.startBackEndSocketIO = function (io, uploader) {
  io.on('connection', (s) => {
    s.once('disconnect', function () {
      clearUpload();
      arrPidObjs = {};
      rmq_speed = 10;
    });

    /* -------------------------------------------- */
    /*           COMPLETE build & run               */
    /* -------------------------------------------- */
    initiateCommandOnTriggered(s, 'pull', 'pullProcess', 'pull.sh', 'Git Pull');
    initiateCommandOnTriggered(s, 'build_logger_player', 'buildLoggerPlayerProcess', 'buildLoggerPlayer.sh', 'Build Logger and Player');
    initiateCommandOnTriggered(s, 'compile_pamela', 'compilePamelaProcess', 'compilePamelaModel.sh', 'Compile Pamela Model');
    initiateCommandOnTriggered(s, 'build_all', 'builldAllProcess', 'buildAllComponents.sh', 'Build DOLL & Genesis components');
    initiateCommandOnTriggered(s, 'run_all', 'runComponentsProcess', 'runAllComponents.sh', 'Run DOLL & Genesis components');
    s.on('stop_current_tasks', (data) => {
      stopAllProcesses(s);
    });

    /* ------------------------------------- */
    /*            AC Controller              */
    /* ------------------------------------- */
    s.on('ac_selected', (acName) => {
      const outMsg = {
        'received-routing-key': 'ac-controller',
        'routing-key': 'ac-controller',
        'app-id': 'control-panel',
        exchange: 'rita',
        data: {
          [acName]: true,
        },
      };
      establishConnAndSendMessage('rita', outMsg['routing-key'], outMsg);
    });

    s.on('ac_deselected', (acName) => {
      const outMsg = {
        'routing-key': 'ac-controller',
        'app-id': 'control-panel',
        data: {
          [acName]: false,
        },
      };
      establishConnAndSendMessage('rita', outMsg['routing-key'], outMsg);
    });

    s.on('acs_status', (data) => {
      console.log(data);
      const outMsg = {
        'routing-key': 'ac-controller',
        'app-id': 'control-panel',
        data: data,
      };
      establishConnAndSendMessage('rita', outMsg['routing-key'], outMsg);
    });

    /* -------------------------------------------- */
    /*         Build components separately          */
    /* -------------------------------------------- */
    initiateCommandOnTriggered(s, 'build_DOLL', 'buildDollProcess', 'buildDOLLComponents.sh', 'Build All DOLL components');
    initiateCommandOnTriggered(s, 'build_se', 'buildSEProcess', 'buildSE.sh', 'Build State Estimation Component');
    initiateCommandOnTriggered(s, 'build_gp', 'buildGPProcess', 'buildGP.sh', 'Build Generative Planner Component');
    initiateCommandOnTriggered(s, 'build_tp', 'buildTPProcess', 'buildTP.sh', 'Build Temporal Planner Component');
    initiateCommandOnTriggered(s, 'build_md', 'buildMDProcess', 'buildMD.sh', 'Build Mission Dispatcher Component');
    initiateCommandOnTriggered(s, 'build_mt', 'buildMTProcess', 'buildMT.sh', 'Build Mission Tracker Component');
    initiateCommandOnTriggered(s, 'build_pg', 'buildPGProcess', 'buildPG.sh', 'Build Prediction Generation Component');
    initiateCommandOnTriggered(s, 'build_ec', 'buildECProcess', 'buildEC.sh', 'Build Experiment Control Component');
    initiateCommandOnTriggered(s, 'build_mqt2rmq', 'buildMqt2rmqProcess', 'buildMqt2rmq.sh', 'Build MQT to RMQ Component');

    /* -------------------------------------------- */
    /*           Run components separately          */
    /* -------------------------------------------- */
    // Yang's component: Without UI
    s.on('run_tr0', (data) => {
      runTR(s, 0);
    });

    // Yang's component: With UI
    s.on('run_tr1', (data) => {
      runTR(s, 1);
    });

    // DOLL components:
    initiateCommandOnTriggered(s, 'run_se', 'runSEProcess', 'runSE.sh', 'Run State Estimation Component');
    initiateCommandOnTriggered(s, 'run_gp', 'runGPProcess', 'runGP.sh', 'Run Generative Planner Component');
    initiateCommandOnTriggered(s, 'run_tp', 'runTPProcess', 'runTP.sh', 'Run Temporal Planner Component');
    initiateCommandOnTriggered(s, 'run_md', 'runMDProcess', 'runMD.sh', 'Run Mission Dispatcher Component');
    initiateCommandOnTriggered(s, 'run_mt', 'runMTProcess', 'runMT.sh', 'Run Mission Tracker Component');
    initiateCommandOnTriggered(s, 'run_pg', 'runPGProcess', 'runPG.sh', 'Run Prediction Generation Component');
    initiateCommandOnTriggered(s, 'run_mqt2rmq', 'runMqt2rmqProcess', 'runMqt2rmq.sh', 'Run MQT to RMQ Component');

    /* -------------------------------------------- */
    /*       Terminate the selected component       */
    /* -------------------------------------------- */
    initiateStopComponentOnTriggered(s, 'stop_tr', 'runTRProcess');
    initiateStopComponentOnTriggered(s, 'stop_se', 'runSEProcess');
    initiateStopComponentOnTriggered(s, 'stop_gp', 'runGPProcess');
    initiateStopComponentOnTriggered(s, 'stop_tp', 'runTPProcess');
    initiateStopComponentOnTriggered(s, 'stop_md', 'runMDProcess');
    initiateStopComponentOnTriggered(s, 'stop_mt', 'runMTProcess');
    initiateStopComponentOnTriggered(s, 'stop_pg', 'runPGProcess');
    initiateStopComponentOnTriggered(s, 'stop_mqt2rmq', 'runMqt2rmqProcess');

    /* -------------------------------------------- */
    /*            Player & Logger & EC              */
    /* -------------------------------------------- */
    // 1. Player
    // Listening for new uploaded file
    uploader.dir = './public/upload/'; // where to save the file to
    uploader.listen(s); // Start listening for file inputs
    uploader.on('error', function (event) {
      // Error handler:
      s.emit('run_player', 'Failed to upload data, please check the console for more info.');
      console.log('Error from uploader', event);
    });

    // Start the Experiment Control
    s.on('run_ec', (dataFileName) => {
      startEC(s, dataFileName, 'exp-0002');
    });

    // Customize the speed before sending data
    s.on('speed_input', (speed) => {
      rmq_speed = speed;
      console.log(`The RMQ replayer is set to speed ${rmq_speed}`);
    });

    // Start the player
    s.on('run_player', (dataFileName) => {
      startRMQPlayer(s, dataFileName, rmq_speed);
    });

    // Stop the player
    initiateStopComponentOnTriggered(s, 'stop_player', 'runPlayerProcess');

    // 2. Logger
    // Start the logger
    s.on('run_logger', (data) => {
      runLogger(s, data);
    });

    // Stop the logger
    initiateStopComponentOnTriggered(s, 'stop_logger', 'runLoggerProcess');
  });
};

/* ------------------------------------------- */
/*           SUPPORTING FUNCTIONS              */
/* ------------------------------------------- */
const startProcess = (script, file, speed) => {
  const spawn = require('child_process').spawn;
  if (file === undefined) {
    return spawn('sh', [`./script/${script}`]);
  }

  if (speed === undefined) {
    return spawn('sh', [`./script/${script}`, file]);
  }

  return spawn('sh', [`./script/${script}`, speed, file]);
};

const startECProcess = (script, file, experiment) => {
  const spawn = require('child_process').spawn;
  return spawn('sh', [`./script/${script}`, experiment, file]);
};

const initiateStopComponentOnTriggered = (socket, emit_name, process_name) => {
  socket.on(emit_name, (data) => {
    stopProcess(socket, process_name);
  });
};

const stopProcess = (socket, process_name) => {
  if (!arrPidObjs[process_name]) {
    console.log(`${process_name} isn't running`);
    socket.emit('stop_current_tasks', `• • • ${process_name} isn't running`);
  } else {
    terminate(arrPidObjs[process_name], (err) => {
      if (err) {
        console.log(`Oopsy, terminate ${process_name} unsuccessful: ${err}`);
      } else {
        delete arrPidObjs[process_name];
        console.log(`• • • Terminating ${process_name} succeeded`);
        const runningProcess = Object.keys(arrPidObjs).length > 0 ? Object.keys(arrPidObjs) : 'None';
        console.log(`The remaining running processes: ${runningProcess}`);
        socket.emit('stop_current_tasks', `• • • Terminating ${process_name} process succeeded`);
      }
    });
  }
};

const stopAllProcesses = (socket) => {
  if (Object.keys(arrPidObjs).length === 0) {
    socket.emit('stop_current_tasks', '• • • Terminate all processes: There is no ongoing processes');
    console.log('Terminate all processes: There is no ongoing processes');
  } else {
    for (const process_name in arrPidObjs) {
      terminate(arrPidObjs[process_name], (err) => {
        if (err) {
          console.log(`Oopsy, terminate ${process_name} unsuccessful: ${err}`);
        } else {
          delete arrPidObjs[process_name];
          console.log(`• • • Terminating ${process_name} succeeded`);
          socket.emit('stop_current_tasks', `• • • Terminating ${process_name} process succeeded`);
        }
      });
    }
  }
  // https://www.npmjs.com/package/process-list
};

// Delete all uploaded files within the /public/upload directory
async function clearUpload() {
  try {
    // await fs.unlink(path.join(__dirname, `../public/upload/${fileName}`));
    await fs.emptyDir(path.join(__dirname, '../public/upload'));
    console.log('delete success!');
  } catch (err) {
    console.log('delete unsuccess!');
    console.error(err);
  }
}

const initiateCommandOnTriggered = (socket, emit_name, process_name, script_name, command_name, trMode) => {
  socket.on(emit_name, function (data) {
    runCommand(socket, emit_name, process_name, script_name, command_name, trMode);
  });
};

/* On error, error will be an instance of Error. 
The error.code property will be the exit code of the process. 
By convention, any exit code other than 0 indicates an error. 
error.signal will be the signal that terminated the process.*/
const runCommand = (socket, emit_name, process_name, script_name, command_name, trMode) => {
  console.log(`--------- Node App (Start Command): ${command_name} component. --------- `);
  socket.emit(`${emit_name}`, `• • • Doing "${command_name}" command, please wait . . .  ----`);
  let logConsoleStream = fs.createWriteStream(`./public/data/output/logFile_${process_name}.log`, { flags: 'a' });
  // let logErrorStream = fs.createWriteStream(`./logErrorFile_${process_name}.log`, {flags: 'a'});

  // Start the process
  const newProcess = startProcess(`${script_name}`);
  arrPidObjs[process_name] = newProcess.pid;

  if (newProcess.error) {
    console.error(`${command_name} (Exec error): ${newProcess.error}`);
  }

  // Store terminal log to file
  newProcess.stdin.pipe(logConsoleStream);
  newProcess.stdout.pipe(logConsoleStream);
  newProcess.stderr.pipe(logConsoleStream);

  // Consuming data STDOUT (from terminal)
  newProcess.stdout.on('data', (data) => {
    processData = data.toString().toLowerCase();
    console.log(`${command_name}: ${processData}`);

    switch (emit_name) {
      case 'build_all':
        if (processData.lastIndexOf('successfully tagged', 0) === 0) {
          socket.emit(`${emit_name}`, `${command_name}: ${data} . . .`);
        }
        break;

      case 'build_logger_player':
        if (processData.lastIndexOf('creating uber jar for rmq-logger', 0) === 0) {
          socket.emit('build_logger_player', 'Building the logger . . .');
        }
        if (processData.lastIndexOf('creating uber jar for rmq-log-player', 0) === 0) {
          socket.emit('build_logger_player', 'Building the player . . .');
        }
        break;

      // Default = components
      default:
        if (processData.includes('app state')) {
          socket.emit(`${emit_name}`, `${command_name} is up and running.`);
        }
    }
  });

  // Consuming data STDERR (from terminal)
  newProcess.stderr.on('data', (data) => {
    processData = data.toString().toLowerCase();
    console.error(`${command_name}: ${processData}`);

    switch (emit_name) {
      case 'build_all':
        if (processData.lastIndexOf('building', 0) === 0) {
          socket.emit(`${emit_name}`, `${command_name}: ${data} . . .`);
        }
        break;
      case 'build_DOLL':
        if (processData.lastIndexOf('adding', 0) === 0) {
          socket.emit(`${emit_name}`, `${command_name}: ${data}, please wait . . .`);
        } else if (processData.lastIndexOf('compiling', 0) === 0) {
          socket.emit('build_DOLL', `${command_name}: ${data}`);
        }
        break;
    }
  });

  // Process exits
  newProcess.on('exit', (code, signal) => {
    if (arrPidObjs[process_name]) delete arrPidObjs[process_name];

    if (code === 0) {
      socket.emit(`${emit_name}`, `• • • Exited/Finished ${command_name} command.`);
    } else if (code === null) {
      socket.emit(`${emit_name}`, `• • • Exited/Finished ${command_name} command. Terminated by user.`);
    } else {
      socket.emit(`${emit_name}`, `• • • Exited/Finished ${command_name} command with error. Please check console for more info.`);
      console.error(`Node App (Exec error): ${newProcess.error}`);
    }

    console.log(`Node App (${command_name}): Finished. Exit code is ${code}. Signal is ${signal}`);
    const runningProcess = Object.keys(arrPidObjs).length > 0 ? Object.keys(arrPidObjs) : 'None';
    console.log(`The remaining running processes: ${runningProcess}`);
  });
};

/* NEED TO REFACTOR... */
const runTR = (socket, mode) => {
  console.log('--------- Node App (Start Command): Run Temporal Reasoning (tom) component. --------- ');
  socket.emit('run_tr', '• • • Running Temporal Reasoning (tom) component . . .  ---- ');
  let logConsoleStream = fs.createWriteStream(`./public/data/output/logFile_runTRProcess.log`, { flags: 'a' });

  const runTRProcess = startProcess(`runTR${mode}.sh`);
  arrPidObjs['runTRProcess'] = runTRProcess.pid;

  if (runTRProcess.error) {
    console.error(`Run Temporal Reasoning (tom) (Exec error): ${runTRProcess.error}`);
  }

  // Store terminal log to file
  runTRProcess.stdin.pipe(logConsoleStream);
  runTRProcess.stdout.pipe(logConsoleStream);
  runTRProcess.stderr.pipe(logConsoleStream);

  runTRProcess.stdout.on('data', (data) => {
    processData = data.toString().toLowerCase();
    console.log(`Run Temporal Reasoning (tom): ${processData}`);
  });

  runTRProcess.stderr.on('data', (data) => {
    processData = data.toString().toLowerCase();
    console.error(`Run Temporal Reasoning (tom): ${processData}`);
  });

  runTRProcess.on('exit', (code) => {
    if (arrPidObjs['runTRProcess']) delete arrPidObjs['runTRProcess'];

    if (code === 0) {
      socket.emit('run_tr', '• • • Exited Temporal Reasoning (tom) component');
    } else if (code === null) {
      socket.emit('run_tr', '• • • Exited Temporal Reasoning (tom) component');
    } else {
      socket.emit('run_tr', 'Exited Temporal Reasoning (tom) component with error)');
      console.error(`Node App (Exec error): ${runTRProcess.error}`);
    }

    console.log(`Node App (Run Temporal Reasoning (tom)): Finished. Exit code is ${code}.`);
    const runningProcess = Object.keys(arrPidObjs).length > 0 ? Object.keys(arrPidObjs) : 'None';
    console.log(`The remaining running processes: ${runningProcess}`);
  });
};

const runLogger = (socket, fileName) => {
  console.log('--------- Node App (Start Command): Run Logger. --------- ');
  socket.emit('run_logger', `• • • Saving output data as ${fileName} located in the public/data/ouput directory . . .`);
  let logConsoleStream = fs.createWriteStream(`./public/data/output/logFile_runLogger.log`, { flags: 'a' });

  const runLoggerProcess = startProcess('saveLogger.sh', fileName.toString());
  arrPidObjs['runLoggerProcess'] = runLoggerProcess.pid;

  if (runLoggerProcess.error) {
    console.error(`Run Logger (Exec error): ${runLoggerProcess.error}`);
  }

  runLoggerProcess.stdin.pipe(logConsoleStream);
  runLoggerProcess.stdout.pipe(logConsoleStream);
  runLoggerProcess.stderr.pipe(logConsoleStream);

  runLoggerProcess.stdout.on('data', (data) => {
    processData = data.toString().toLowerCase();
    console.log(`Logger: ${processData}`);
  });

  runLoggerProcess.stderr.on('data', (data) => {
    processData = data.toString().toLowerCase();
    console.error(`Logger: ${processData}`);
  });

  runLoggerProcess.on('exit', (code) => {
    if (arrPidObjs['runLoggerProcess']) delete arrPidObjs['runLoggerProcess'];

    if (code === 0) {
      socket.emit('run_logger', `• • • Finished saving data to a file named ${fileName}`);
    } else if (code === null) {
      socket.emit('run_logger', `• • • Finished saving data to a file named ${fileName}`);
    } else {
      console.error(`Node App: Exec error: ${runLoggerProcess.error}`);
      socket.emit('run_logger', '• • • Failed to save data, please check the console for more info.');
    }

    console.log(`Node App (Run Logger): Exit code is ${code}`);
    console.log('--------- Node App (Finish command): Run Logger. --------- ');
    const runningProcess = Object.keys(arrPidObjs).length > 0 ? Object.keys(arrPidObjs) : 'None';
    console.log(`The remaining running processes: ${runningProcess}`);
  });
};

const startEC = (socket, fileName, experiment) => {
  console.log('--------- Node App (Start Command): Run RMQ Player. --------- ');
  socket.emit('run_ec', `• • • Doing "Run Experiment Control" command, please wait . . .  . . .`);
  let logConsoleStream = fs.createWriteStream(`./public/data/output/logFile_runECProcess.log`, { flags: 'a' });

  const runECProcess = startECProcess('runEC.sh', fileName.toString(), experiment);
  arrPidObjs['runECProcess'] = runECProcess.pid;
  if (runECProcess.error) {
    console.error(`Run Experiment Control (Exec error): ${runECProcess.error}`);
  }

  runECProcess.stdin.pipe(logConsoleStream);
  runECProcess.stdout.pipe(logConsoleStream);
  runECProcess.stderr.pipe(logConsoleStream);

  runECProcess.stdout.on('data', (data) => {
    processData = data.toString().toLowerCase();
    console.log(`Run Experiment Control: ${processData}`);
  });

  runECProcess.stderr.on('data', (data) => {
    processData = data.toString().toLowerCase();
    console.error(`Run Experiment Control: ${processData}`);
  });

  runECProcess.on('exit', (code) => {
    if (arrPidObjs['runECProcess']) delete arrPidObjs['runECProcess'];

    if (code === 0) {
      socket.emit('run_ec', `• • • Finished running Experiment Control`);
    } else if (code === null) {
      socket.emit('run_ec', `• • • The Experiment Control is terminated by user.`);
    } else {
      console.error(`Node App: Exec error: ${runECProcess.error}`);
      socket.emit('run_ec', '• • • Failed to run the Experiment Control, please check the console for more info.');
    }
    console.log(`Node App (Run EC): Exit code is ${code}`);
    console.log('--------- Node App (Finish command): Run EC. --------- ');
    const runningProcess = Object.keys(arrPidObjs).length > 0 ? Object.keys(arrPidObjs) : 'None';
    console.log(`The remaining running processes: ${runningProcess}`);
  });
};

const startRMQPlayer = (socket, fileName, speed) => {
  console.log('--------- Node App (Start Command): Run RMQ Player. --------- ');
  socket.emit('run_player', `• • • Sending data (Speed: ${speed}) from ${fileName} . . .`);
  let logConsoleStream = fs.createWriteStream(`./public/data/output/logFile_startRMQPlayer.log`, { flags: 'a' });

  let startPlayerProcess = null;
  if (speed === 10) {
    // default = 10
    startPlayerProcess = startProcess('rmqPlayer.sh', fileName.toString());
    console.log(fileName.toString());
  } else {
    rmq_speed = 10;
    console.log(`The speed is set back to default: 10x`);
    startPlayerProcess = startProcess('rmqPlayerSpeed.sh', fileName.toString(), speed);
  }

  arrPidObjs['runPlayerProcess'] = startPlayerProcess.pid;

  if (startPlayerProcess.error) {
    console.error(`Run RMQ Player (Exec error): ${startPlayerProcess.error}`);
  }

  startPlayerProcess.stdin.pipe(logConsoleStream);
  startPlayerProcess.stdout.pipe(logConsoleStream);
  startPlayerProcess.stderr.pipe(logConsoleStream);

  startPlayerProcess.stdout.on('data', (data) => {
    processData = data.toString().toLowerCase();
    console.log(`RMQ Player: ${processData}`);
  });

  startPlayerProcess.stderr.on('data', (data) => {
    processData = data.toString().toLowerCase();
    console.error(`RMQ Player: ${processData}`);

    if (processData.lastIndexOf('exception', 0) === 0) {
      console.error(`Node App: Exec error: ${startPlayerProcess.error}`);
      socket.emit('run_player', '• • • Failed to send data, please check the console for more info.');
      clearUpload();
    }
  });

  startPlayerProcess.on('exit', (code) => {
    if (arrPidObjs['runPlayerProcess']) delete arrPidObjs['runPlayerProcess'];

    if (code === 0) {
      socket.emit('run_player', `• • • Finished sending data from the ${fileName}`);
      clearUpload();
    } else if (code === null) {
      socket.emit('run_player', `• • • The rmq player is terminated by user.`);
      clearUpload();
    } else {
      console.error(`Node App: Exec error: ${startPlayerProcess.error}`);
      socket.emit('run_player', '• • • Failed to send data, please check the console for more info.)');
      clearUpload();
    }
    console.log(`Node App (Run RMQ Player): Exit code is ${code}`);
    console.log('--------- Node App (Finish command): Run RMQ Player. --------- ');
    const runningProcess = Object.keys(arrPidObjs).length > 0 ? Object.keys(arrPidObjs) : 'None';
    console.log(`The remaining running processes: ${runningProcess}`);
  });
};
