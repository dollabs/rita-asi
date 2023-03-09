/* eslint-disable no-undef */
/* eslint-disable camelcase */
/* eslint-disable no-restricted-syntax */
const Phaser = require('phaser');
const Game = require('./scenes/Game.js');
const Preloader = require('./scenes/Preload.js');
const socketFrontEnd = io.connect('http://localhost:3000/map');
exports.socketFrontEnd = socketFrontEnd;

/*---------------------------------------------------*/
/*----- 2D MAP INFORMATION for PHASER CANVAS ------*/
/*---------------------------------------------------*/
// 1. Create and export Phaser Canvas with config info
const config = {
  type: Phaser.AUTO,
  width: 1800,
  height: 1000,
  parent: 'map',
  backgroundColor: '#ffffff',
  pixelArt: true,
  physics: {
    default: 'arcade',
    arcade: {
      debug: true,
    },
  },
  // resolution: window.devicePixelRatio,
  scene: [Preloader, Game],
  scale: {
    zoom: 0.7,
    mode: Phaser.Scale.NONE,
  },
};
const newPhaserWindow = new Phaser.Game(config);
exports.newPhaserWindow = newPhaserWindow;

// 2. Manually added coordinates for Minecraft maps
// a) MAP Boundary coordinates
// get info from Map-processedData.json file:
// minXFloor (x_left), maxXFloor (x-right), minZFloor (z-top), maxZFloor (z-bottom)
exports.map_coordinates = {
  Falcon: { x_left: -2112, x_right: -2020, z_top: 143, z_bottom: 207 },
  Sparky: { x_left: -2176, x_right: -2108, z_top: 144, z_bottom: 199 },
  Saturn1_1: { x_left: -2225, x_right: -2087, z_top: -11, z_bottom: 128 },
  Saturn1_6: { x_left: -2225, x_right: -2087, z_top: -11, z_bottom: 128 },
  Saturn2_0: { x_left: -2225, x_right: -2087, z_top: -11, z_bottom: 78 },
  Saturn2_6: { x_left: -2225, x_right: -2087, z_top: -11, z_bottom: 78 },
};

//  b) ROOM Boundary coordinates for Falcon
exports.room_coordinates = {
  //(x, z)
  FalconLobby: { l: -2095, r: -2089, t: 144, b: 150 },
  FalconSO: { l: -2088, r: -2085, t: 144, b: 151 },
  FalconBreakR: { l: -2084, r: -2076, t: 144, b: 151 },
  FalconExecS1: { l: -2075, r: -2059, t: 144, b: 151 },
  FalconExecS2: { l: -2058, r: -2044, t: 144, b: 151 },
  FalconKO: { x: -2043, x1: -2028, y: 144, x2: -2028, x3: -2036, y2: 159, x4: -2036, x5: -2043, y3: 151 },
  FalconKT: { l: -2027, r: -2020, t: 144, b: 159 },
  FalconMCR: { l: -2050, r: -2042, t: 157, b: 178 },
  FalconSCR1: { l: -2064, r: -2056, t: 157, b: 167 },
  FalconSCR2: { l: -2064, r: -2056, t: 168, b: 178 },
  FalconWomRR: { l: -2072, r: -2066, t: 170, b: 178 },
  FalconMenRR: { l: -2072, r: -2066, t: 161, b: 169 },
  FalconRoomJ: { l: -2072, r: -2066, t: 157, b: 160 },
  FalconCFarm: { l: -2095, r: -2078, t: 157, b: 178 },
  FalconR101: { l: -2036, r: -2028, t: 161, b: 169 },
  FalconR102: { l: -2036, r: -2028, t: 170, b: 178 },
  FalconR103: { l: -2036, r: -2028, t: 184, b: 192 },
  FalconR104: { l: -2045, r: -2037, t: 184, b: 192 },
  FalconR105: { l: -2054, r: -2046, t: 184, b: 192 },
  FalconR106: { l: -2063, r: -2055, t: 184, b: 192 },
  FalconR107: { l: -2072, r: -2064, t: 184, b: 192 },
  FalconR108: { l: -2081, r: -2073, t: 184, b: 192 },
  FalconR109: { l: -2090, r: -2082, t: 184, b: 192 },
  FalconR110: { l: -2099, r: -2091, t: 184, b: 192 },
  FalconR111: { l: -2108, r: -2100, t: 184, b: 192 },
};
// c) Mapping rooms 'name (SE component to RITA UI component)
exports.mappingName_SE_UI = {
  Lobby: 'FalconLobby',
  CloakR: 'FalconSO',
  BreakR: 'FalconBreakR',
  ExecS1: 'FalconExecS1',
  ExecS2: 'FalconExecS2',
  CSNorth: 'FalconKO',
  CSEast: 'FalconKO',
  Terrance: 'FalconKT',
  MCR: 'FalconMCR',
  SCR1: 'FalconSCR1',
  SCR2: 'FalconSCR2',
  WomRR: 'FalconWomRR',
  MenRR: 'FalconMenRR',
  RoomJ: 'FalconRoomJ',
  Cfarm: 'FalconCFarm',
  Room101: 'FalconR101',
  Room102: 'FalconR102',
  Room103: 'FalconR103',
  Room104: 'FalconR104',
  Room105: 'FalconR105',
  Room106: 'FalconR106',
  Room107: 'FalconR107',
  Room108: 'FalconR108',
  Room109: 'FalconR109',
  Room110: 'FalconR110',
  Room111: 'FalconR111',
};

// 3. IMAGE mapping
exports.block_to_texture = {
  // Passable
  passable: 'passable.png',

  // Blockage
  bedrock: 'bedrock.png',
  gravel: 'gravel.png',
  frozen: 'frozen.png',
  redstone: 'redstone.png',

  // Doors
  open_door: 'door_open.png',

  // Player
  redPlayer: 'redPlayer.png',
  bluePlayer: 'bluePlayer.png',
  greenPlayer: 'greenPlayer.png',
  frozenPlayer: 'freeze_player.png',

  // Victims
  yellow_victim: 'victim-yellow.gif',
  green_victim: 'victim-green.gif',
  red_victim: 'victim-red.gif',
  green_victim_triaged: 'victim-green-triaged.gif',
  yellow_victim_triaged: 'victim-yellow-triaged.gif',

  // old makrers
  marker_blue1: 'block_marker_blue_1.png',
  marker_blue2: 'block_marker_blue_2.png',
  marker_blue3: 'block_marker_blue_3.png',
  marker_green1: 'block_marker_green_1.png',
  marker_green2: 'block_marker_green_2.png',
  marker_green3: 'block_marker_green_3.png',
  marker_red1: 'block_marker_red_1.png',
  marker_red2: 'block_marker_red_2.png',
  marker_red3: 'block_marker_red_3.png',

  // new markers
  marker_blue_abrasion: 'block_marker_blue_abrasion.png',
  marker_blue_bonedamage: 'block_marker_blue_bonedamage.png',
  marker_blue_critical: 'block_marker_blue_critical.png',
  marker_blue_criticalvictim: 'block_marker_blue_criticalvictim.png',
  marker_blue_novictim: 'block_marker_blue_novictim.png',
  marker_blue_regularvictim: 'block_marker_blue_regularvictim.png',
  marker_blue_rubble: 'block_marker_blue_rubble.png',
  marker_blue_sos: 'block_marker_blue_sos.png',
  marker_blue_threat: 'block_marker_blue_threat.png',
  marker_blue_wildcard: 'block_marker_blue_wildcard.png',
  marker_green_abrasion: 'block_marker_green_abrasion.png',
  marker_green_bonedamage: 'block_marker_green_bonedamage.png',
  marker_green_critical: 'block_marker_green_critical.png',
  marker_green_criticalvictim: 'block_marker_green_criticalvictim.png',
  marker_green_novictim: 'block_marker_green_novictim.png',
  marker_green_regularvictim: 'block_marker_green_regularvictim.png',
  marker_green_rubble: 'block_marker_green_rubble.png',
  marker_green_sos: 'block_marker_green_sos.png',
  marker_green_threat: 'block_marker_green_threat.png',
  marker_green_wildcard: 'block_marker_green_wildcard.png',
  marker_red_abrasion: 'block_marker_red_abrasion.png',
  marker_red_bonedamage: 'block_marker_red_bonedamage.png',
  marker_red_critical: 'block_marker_red_critical.png',
  marker_red_criticalvictim: 'block_marker_red_criticalvictim.png',
  marker_red_novictim: 'block_marker_red_novictim.png',
  marker_red_regularvictim: 'block_marker_red_regularvictim.png',
  marker_red_rubble: 'block_marker_red_rubble.png',
  marker_red_sos: 'block_marker_red_sos.png',
  marker_red_threat: 'block_marker_red_threat.png',
  marker_red_wildcard: 'block_marker_red_wildcard.png',
};

// 4. Victims, updated in the Game.js file
const victims = {
  critical: 0, // old: yellow
  regular: 0, // old: green
};
exports.victims = victims;

/*----------------------------------*/
/*----- Socket Keys Mapping --------*/
/*----------------------------------*/
const socket_key = {
  // key = map_id, value = socket_id
  trial_start: 'trial_start',
  trial_stop: 'trial_stop',
  mission_start: 'mission_start',
  mission_stop: 'mission_stop',
  mission_timer: 'mission_timer',
  groundtruth_victim_list: 'victim_list',
  groundtruth_blockage_list: 'blockage_list',
  groundtruth_freezeBlock_list: 'freezeBlock_list',
  groundtruth_threatsign_list: 'threatsign_list',
  groundtruth_victims_expired: 'victims_timeout',
  groundtruth_all_victims_rescued: 'victims_rescued',
  event_door: 'event_door',
  event_triage: 'event_triage',
  event_evacuation: 'event_evacuation',
  event_pause: 'pause',
  event_score: 'event_scoreBoard',
  event_victimPlaced: 'event_victimPlaced',
  event_victimPickedUp: 'event_victimPickedUp',
  event_roleChange: 'event_roleChange',
  event_markerPlaced: 'event_markerPlaced',
  event_markerRemoved: 'event_markerRemoved',
  event_rubbleDestroyed: 'event_rubbleDestroyed',
  event_rubbleCollapse: 'event_rubbleCollapse',
  event_frozen: 'event_playerFrozen',
  event_perturbation: 'event_perturbation',
  observation_player: 'player_position',
  observation_player1: 'player1_position',
  observation_player2: 'player2_position',
  observation_player3: 'player3_position',
  rl_robot_pos: 'robot_pos',
  prediction_next_room: 'next_room_predictions',
  prediction_story_understanding: 'story_understanding_predictions',
  prediction_score: 'final_score_predictions',
};
exports.socketKey = socket_key;

/*---------------------------------------------------*/
/*--------- WEBPAGE UI (not PHASER CANVAS) ----------*/
/*---------------------------------------------------*/
// DOM elements
const DOM = {
  trial_name: $('#trial_name'),
  trial_number: $('#trial_number'),
  trial_condition: $('#trial_condition'),
  trial_date: $('#trial_date'),
  subject_id: $('#subject_id'),
  experiment_name: $('#experiment_name'),
  experiment_mission: $('#mission_name'),
  testbed_version: $('#testbed_v'),

  mission_timer: $('#mission_timer'),
  groundtruth_score: $('#groundtruth-teamScore'),
  prediction_score: $('#prediction-teamScore'),
  pause: $('#pause'),
  triaged_critical_victims: $('#triaged-critical'),
  triaged_regular_victims: $('#triaged-regular'),
  saved_critical_victims: $('#saved-critical'),
  saved_regular_victims: $('#saved-regular'),
  story_understanding: $('#story'),

  perturbation_status: $('#perturbation-stat'),
  perturbation_type: $('#perturbation-type'),
};

// Update data as we receive new data from Socket backend
socketFrontEnd.on(socket_key.trial_start, function (data) {
  const info = data.data;
  DOM.trial_name.text(`Trial name: ${info.name}`);
  DOM.trial_number.text(`Trial number: ${info.trial_number}`);
  DOM.trial_condition.text(`Trial condition: ${info.condition}`);
  DOM.trial_date.text(`Trial date: ${info.trial_date}`);
  DOM.subject_id.text(`Subject ID: ${info.subjects}`);
  DOM.experiment_name.text(`Experiment name: ${info.experiment_name}`);
  DOM.experiment_mission.text(`Mission name: ${info.experiment_mission}`);
  DOM.testbed_version.text(`Testbed version: ${info.testbed_version}`);

  // Resetting scene... NEED WORK
  console.log('TRIAL_START');
});

socketFrontEnd.on(socket_key.prediction_story_understanding, function (data) {
  let message;
  if (data.action === 'PM1-story-selection') {
    message = `Action: ${data.action} || Story: ${data.story} || Object: ${data.object}`;
  } else if (data.action === 'PM2-story-event-prediction') {
    message = `Action: ${data.action} || Story: ${data.story} || Subject: ${data.subject}`;
  } else if (data.action === 'PM3-story-player-prediction') {
    message = `Action: ${data.action} || Story: ${data.story}`;
  } else if (data.action === 'PM4-story-communication-prediction') {
    message = `Action: ${data.action} || Story: ${data.story} || Subject: ${data.subject}`;
  } else if (data.action === 'PM5-story-player-motivation') {
    message = `Action: ${data.action} || Story: ${data.story}`;
  }
  message = message.replaceAll('_', ' ');
  message = message.replaceAll('-', ' ');
  DOM.story_understanding.prepend(`<p>${message}</p>`).addClass('typingAnimation');
});

socketFrontEnd.on(socket_key.mission_timer, function (data) {
  DOM.mission_timer.text(data);
});

socketFrontEnd.on(socket_key.event_pause, function (data) {
  DOM.pause.text(data);
});

socketFrontEnd.on(socket_key.event_score, function (data) {
  DOM.groundtruth_score.text(data);
});

socketFrontEnd.on(socket_key.prediction_score, function (data) {
  DOM.prediction_score.text(data.object);
});

socketFrontEnd.on(socket_key.event_triage, function (data) {
  DOM.triaged_regular_victims.text(`Triaged Regular: ${data.triagedRegular} out of ${victims.regular}`);
  DOM.triaged_critical_victims.text(`Triaged Critical: ${data.triagedCritical} out of ${victims.critical}`);
});

socketFrontEnd.on(socket_key.event_evacuation, function (data) {
  DOM.saved_regular_victims.text(`Saved Regular: ${data.savedRegular} out of ${victims.regular}`);
  DOM.saved_critical_victims.text(`Saved Critical: ${data.savedCritical} out of ${victims.critical}`);
});

socketFrontEnd.on(socket_key.event_perturbation, function (data) {
  DOM.perturbation_status.text(`Status: ${data.state}`);
  DOM.perturbation_type.text(`Type: ${data.type}`);
});

if (performance.navigation.type === performance.navigation.TYPE_RELOAD) {
  socketFrontEnd.emit('clearState', 'Please clear the current states');
  victims.regular = 0;
  victims.critical = 0;
  console.info('This page is reloaded');
} else {
  console.info('This page is not reloaded');
}
