// Make socketIO connection
const socketFrontEnd = io.connect('http://localhost:8080/');
var siofu = new SocketIOFileUpload(socketFrontEnd);
var fileName = null;
/* --------------------------------------- */
/*               DOM Naming                */
/* --------------------------------------- */
var terminal_message = $('.fakeScreen');

// Complete build & run
var pull = $('#pull');
var build_logger_player = $('.build-logger-player');
var compile_pamela = $('.compile-pamela');
var build_all = $('#build-all');
var run_all = $('#run-all');
var stop_current_tasks = $('#stop-current-tasks');

// Build components separately
var build_DOLL = $('#build-DOLL');
var build_se = $('#build-se');
var build_gp = $('#build-gp');
var build_tp = $('#build-tp');
var build_md = $('#build-md');
var build_mt = $('#build-mt');
var build_pg = $('#build-pg');
var build_ec = $('#build-ec');
var build_mqt2rmq = $('#build-mqt2rmq');

// Run components separately
var run_tr0 = $('#run-tr0');
var run_tr1 = $('#run-tr1');
var run_se = $('#run-se');
var run_gp = $('#run-gp');
var run_tp = $('#run-tp');
var run_md = $('#run-md');
var run_mt = $('#run-mt');
var run_pg = $('#run-pg');
var run_ec = $('#run-ec');
var run_mqt2rmq = $('#run-mqt2rmq');

// Stop components separately
var stop_tr = $('#stop-tr');
var stop_se = $('#stop-se');
var stop_gp = $('#stop-gp');
var stop_tp = $('#stop-tp');
var stop_md = $('#stop-md');
var stop_mt = $('#stop-mt');
var stop_pg = $('#stop-pg');
var stop_mqt2rmq = $('#stop-mqt2rmq');

// Run Logger and player
var set_player_speed = $('#set-player-speed');
var run_logger = $('#run-logger');
var run_player = $('#run-player');

// Stop Logger and Player
var stop_logger = $('#stop-logger');
var stop_player = $('#stop-player');

// ACs
var reset_acs = $('#reset-acs');

/* ----------------------------------------------- */
/*   • Wait for actions from the frontend 
/*   • Send (Emit) signal to the backend         
/* ------------------------------------------------ */
function sendCommandOnTrigger(command, emitKey) {
  command.click(function () {
    socketFrontEnd.emit(emitKey);
  });
}

// Complete build & run
sendCommandOnTrigger(pull, 'pull');
sendCommandOnTrigger(build_logger_player, 'build_logger_player');
sendCommandOnTrigger(compile_pamela, 'compile_pamela');
sendCommandOnTrigger(build_all, 'build_all');
sendCommandOnTrigger(run_all, 'run_all');
sendCommandOnTrigger(stop_current_tasks, 'stop_current_tasks');

// Build components separately
sendCommandOnTrigger(build_DOLL, 'build_DOLL');
sendCommandOnTrigger(build_se, 'build_se');
sendCommandOnTrigger(build_gp, 'build_gp');
sendCommandOnTrigger(build_tp, 'build_tp');
sendCommandOnTrigger(build_md, 'build_md');
sendCommandOnTrigger(build_mt, 'build_mt');
sendCommandOnTrigger(build_pg, 'build_pg');
sendCommandOnTrigger(build_ec, 'build_ec');
sendCommandOnTrigger(build_mqt2rmq, 'build_mqt2rmq');

// Run components separately
sendCommandOnTrigger(run_tr0, 'run_tr0');
sendCommandOnTrigger(run_tr1, 'run_tr1');
sendCommandOnTrigger(run_se, 'run_se');
sendCommandOnTrigger(run_gp, 'run_gp');
sendCommandOnTrigger(run_tp, 'run_tp');
sendCommandOnTrigger(run_md, 'run_md');
sendCommandOnTrigger(run_mt, 'run_mt');
sendCommandOnTrigger(run_pg, 'run_pg');
sendCommandOnTrigger(run_mqt2rmq, 'run_mqt2rmq');

// Stop components separately
sendCommandOnTrigger(stop_tr, 'stop_tr');
sendCommandOnTrigger(stop_se, 'stop_se');
sendCommandOnTrigger(stop_gp, 'stop_gp');
sendCommandOnTrigger(stop_tp, 'stop_tp');
sendCommandOnTrigger(stop_md, 'stop_md');
sendCommandOnTrigger(stop_mt, 'run_md');
sendCommandOnTrigger(stop_pg, 'stop_pg');
sendCommandOnTrigger(stop_mqt2rmq, 'stop_mqt2rmq');

// Run & Stop Logger * Run EC
run_logger.click(function (e) {
  var inputVal = $('#text-input').val();
  if (inputVal.length > 0) {
    socketFrontEnd.emit('run_logger', inputVal);
  }
});

stop_logger.click(function (e) {
  $('#filename-input').trigger('reset');
  socketFrontEnd.emit('stop_logger', 'Please stop the logger');
});

// Upload input file
siofu.listenOnSubmit(document.getElementById('select-file'), document.getElementById('upload_input'));
siofu.addEventListener('progress', function (event) {
  var percent = (event.bytesLoaded / event.file.size) * 100;
  console.log('File is', percent.toFixed(2), 'percent loaded');
});
siofu.addEventListener('complete', function (event) {
  fileName = event.file.name;
  terminal_message.prepend(`<p class="anim-typewriter gradient-text"> > Successfully selected ${fileName} </p>`);
  $('.inputFile').trigger('reset');
});

// EC
run_ec.click(function () {
  if (fileName != null) {
    socketFrontEnd.emit('run_ec', fileName);
  } else {
    terminal_message.prepend(`<p class="anim-typewriter gradient-text"> > No file selected, please pick a file </p>`);
  }
});

// Set the speed for the Player
set_player_speed.click(function (e) {
  var inputVal = $('#speed-input').val();
  $('#speed-input').val('');
  if (inputVal.length > 0) {
    console.log(inputVal);
    socketFrontEnd.emit('speed_input', inputVal);
    terminal_message.prepend(`<p class="anim-typewriter gradient-text"> > The RMQ replayer is set to speed ${inputVal} </p>`);
  }
});

// Run Player
run_player.click(function () {
  if (fileName != null) {
    socketFrontEnd.emit('run_player', fileName);
  } else {
    terminal_message.prepend(`<p class="anim-typewriter gradient-text"> > No file selected, please pick a file </p>`);
  }
});

sendCommandOnTrigger(stop_player, 'stop_player');

// // Drag and Drop option:
// siofu.listenOnDrop(document.getElementById('file_drop'));
// // Do something on upload progress (cool - if we want to do progress bar later):
// siofu.addEventListener('progress', function (event) {
//   var percent = (event.bytesLoaded / event.file.size) * 100;
//   console.log('File is', percent.toFixed(2), 'percent loaded');
// });

/* ----------------------------------------------- */
/*   • Listen and get data from the back end 
/*   • Display the data to the front-end         
/* ------------------------------------------------ */
function displayData(socketKey) {
  socketFrontEnd.on(socketKey, function (data) {
    terminal_message.prepend(`<p class="anim-typewriter gradient-text"> > ${data} </p>`);
  });
}

// Complete build & run
displayData('pull'); // 1. Pull the lastest change
displayData('build_logger_player'); // 2. Pre-built: Logger and Player
displayData('compile_pamela'); // 2. Pre-built: Pamela model for SE
displayData('build_all'); // 3. Full build
displayData('run_all'); // 4. Full run (both DOLL and MIT)
displayData('stop_current_tasks'); // 5. Stop all running processes

// Build components separately
displayData('build_DOLL');
displayData('build_se');
displayData('build_gp');
displayData('build_tp');
displayData('build_md');
displayData('build_mt');
displayData('build_pg');
displayData('build_ec');
displayData('build_mqt2rmq');

// Run components separately
displayData('run_tr');
displayData('run_se');
displayData('run_gp');
displayData('run_tp');
displayData('run_md');
displayData('run_mt');
displayData('run_pg');
displayData('run_ec');
displayData('run_mqt2rmq');

// Stop components separately
displayData('stop_tr');
displayData('stop_se');
displayData('stop_gp');
displayData('stop_tp');
displayData('stop_md');
displayData('stop_mt');
displayData('stop_pg');
displayData('stop_ec');
displayData('stop_mqt2rmq');

// Run Logger
displayData('run_logger');

// Run Player
socketFrontEnd.on('run_player', function (data) {
  fileName = null;
  terminal_message.prepend(`<p class="anim-typewriter gradient-text"> > ${data} </p>`);
});

const getACsAndUpdateSwitchBoxes = function () {
  let ac_aptima_ta3_measures = localStorage.getItem('ac_aptima_ta3_measures');
  let ac_cmu_ta2_beard = localStorage.getItem('ac_cmu_ta2_beard');
  let ac_cmu_ta2_ted = localStorage.getItem('ac_cmu_ta2_ted');
  let ac_cornell_ta2_teamtrust = localStorage.getItem('ac_cornell_ta2_teamtrust');
  let ac_cornell_ta2_asi_facework = localStorage.getItem('ac_cornell_ta2_asi_facework');
  let ac_gallup_ta2_gelp = localStorage.getItem('ac_gallup_ta2_gelp');
  let ac_cmufms_ta2_cognitive = localStorage.getItem('ac_cmufms_ta2_cognitive');
  let ac_rutgers_ta2_utility = localStorage.getItem('ac_rutgers_ta2_utility');
  let ac_ucf_ta2_playerprofile = localStorage.getItem('ac_ucf_ta2_playerprofile');
  let ac_ihmc_ta2 = localStorage.getItem('ac_ihmc_ta2');
  let ac_doll_ta1_utterance = localStorage.getItem('ac_doll_ta1_utterance');
  let ac_doll_ta1_word = localStorage.getItem('ac_doll_ta1_word');

  ac_aptima_ta3_measures = ac_aptima_ta3_measures === 'true' ? true : false;
  $('.ac_aptima_ta3_measures').prop('checked', ac_aptima_ta3_measures);

  ac_cmu_ta2_beard = ac_cmu_ta2_beard === 'true' ? true : false;
  $('.ac_cmu_ta2_beard').prop('checked', ac_cmu_ta2_beard);

  ac_cmu_ta2_ted = ac_cmu_ta2_ted === 'true' ? true : false;
  $('.ac_cmu_ta2_ted').prop('checked', ac_cmu_ta2_beard);

  ac_cornell_ta2_teamtrust = ac_cornell_ta2_teamtrust === 'true' ? true : false;
  $('.ac_cornell_ta2_teamtrust').prop('checked', ac_cornell_ta2_teamtrust);

  ac_cornell_ta2_asi_facework = ac_cornell_ta2_asi_facework === 'true' ? true : false;
  $('.ac_cornell_ta2_asi_facework').prop('checked', ac_cornell_ta2_asi_facework);

  ac_gallup_ta2_gelp = ac_gallup_ta2_gelp === 'true' ? true : false;
  $('.ac_gallup_ta2_gelp').prop('checked', ac_gallup_ta2_gelp);

  ac_cmufms_ta2_cognitive = ac_cmufms_ta2_cognitive === 'true' ? true : false;
  $('.ac_cmufms_ta2_cognitive').prop('checked', ac_cmufms_ta2_cognitive);

  ac_rutgers_ta2_utility = ac_rutgers_ta2_utility === 'true' ? true : false;
  $('.ac_rutgers_ta2_utility').prop('checked', ac_rutgers_ta2_utility);

  ac_ucf_ta2_playerprofile = ac_ucf_ta2_playerprofile === 'true' ? true : false;
  $('.ac_ucf_ta2_playerprofile').prop('checked', ac_ucf_ta2_playerprofile);

  ac_ihmc_ta2 = ac_ihmc_ta2 === 'true' ? true : false;
  $('.ac_ihmc_ta2').prop('checked', ac_ihmc_ta2);

  ac_doll_ta1_utterance = ac_doll_ta1_utterance === 'true' ? true : false;
  $('.ac_doll_ta1_utterance').prop('checked', ac_doll_ta1_utterance);

  ac_doll_ta1_word = ac_doll_ta1_word === 'true' ? true : false;
  $('.ac_doll_ta1_word').prop('checked', ac_doll_ta1_word);

  return {
    ac_aptima_ta3_measures,
    ac_cmu_ta2_beard,
    ac_cmu_ta2_ted,
    ac_cornell_ta2_teamtrust,
    ac_cornell_ta2_asi_facework,
    ac_gallup_ta2_gelp,
    ac_cmufms_ta2_cognitive,
    ac_rutgers_ta2_utility,
    ac_ucf_ta2_playerprofile,
    ac_ihmc_ta2,
    ac_doll_ta1_utterance,
    ac_doll_ta1_word,
  };
};
// AC controller
let ACsStates = getACsAndUpdateSwitchBoxes();
socketFrontEnd.emit('acs_status', ACsStates);

reset_acs.click(function () {
  localStorage.setItem('ac_aptima_ta3_measures', false);
  localStorage.setItem('ac_cmu_ta2_beard', false);
  localStorage.setItem('ac_cmu_ta2_ted', false);
  localStorage.setItem('ac_cornell_ta2_teamtrust', false);
  localStorage.setItem('ac_cornell_ta2_asi_facework', false);
  localStorage.setItem('ac_gallup_ta2_gelp', false);
  localStorage.setItem('ac_cmufms_ta2_cognitive', false);
  localStorage.setItem('ac_rutgers_ta2_utility', false);
  localStorage.setItem('ac_ucf_ta2_playerprofile', false);
  localStorage.setItem('ac_ihmc_ta2', false);
  localStorage.setItem('ac_doll_ta1_utterance', false);
  localStorage.setItem('ac_doll_ta1_word', false);

  ACsStates = getACsAndUpdateSwitchBoxes();
  socketFrontEnd.emit('acs_status', ACsStates);
});

$('input.switchbox').click(function () {
  if ($(this).is(':checked')) {
    socketFrontEnd.emit('ac_selected', $(this).val());
    localStorage.setItem($(this).val(), true);
  } else if ($(this).is(':not(:checked)')) {
    socketFrontEnd.emit('ac_deselected', $(this).val());
    localStorage.setItem($(this).val(), false);
  }
});
