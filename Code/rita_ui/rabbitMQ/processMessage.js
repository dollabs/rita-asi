/* eslint-disable no-plusplus */
/* eslint-disable camelcase */
/* eslint-disable one-var */
/* eslint-disable prefer-destructuring */
const Event = require('events');
// const { get } = require('http');
const Player = require('./models/player.js');
const emit = new Event();

const timeObject = {
  rawTimestamp: null,
  processedTimestamp: null,
  minTimestamp: Infinity,
  missionDurationInSec: null,
  missionDurationInMin: null,
  timeDurationTracker: Infinity, // Graph can't keep up
  previousTimestamp: 0, // Graph can't keep up
};

// ------------------------
// Supporting functions
// ------------------------
const handleRLRobot = (inputData) => {
  const { header, msg, data } = inputData.header;

  setTimeout(function () {
    if (header.message_type === 'observation' && msg.sub_type === 'state') {
      emit.emit('robot_pos', { x: data.x, z: data.z, y: data.y }); // array, current_pos
    } else if (header.message_type === 'event') {
      if (msg.sub_type === 'Event:startRender') {
        const mapName = data.mission;
        emit.emit('trial_start', { name: mapName });
      }
    }
  }, 500);
};

const handleRITAPredictions = (data) => {
  if (data.predictions) {
    if (data['app-id'] === 'StateEstimation' || (data['app-id'] === 'PredictionGenerator' && !data.predictions.state)) {
      // Only accept hypothesis with rank 0 (meaning the most favored prediction)
      if (data.predictions['hypothesis-rank'] === 0) {
        if (data.predictions.action.startsWith('PM')) {
          setTimeout(function () {
            emit.emit('story_understanding_predictions', [data.predictions, timeObject.timeScaleMin]);
          }, 50);
        } else if (data.predictions.action === 'final-score') {
          setTimeout(function () {
            emit.emit('final_score_predictions', [data.predictions, timeObject.timeScaleMin]);
          }, 50);
        } else if (data.predictions.action === 'enter-room') {
          setTimeout(function () {
            emit.emit('next_room_predictions', [data.predictions, timeObject.timeScaleMin]);
          }, 50);
        } else {
          setTimeout(function () {
            emit.emit('newPrediction', [data.predictions, timeObject.timeScaleMin]);
          }, 500);
        }
      }
    }
  }
};

const handleCognitiveLoad = (data, p1, p2, p3) => {
  if (data['belief-state-changes']) {
    if (data['belief-state-changes'].changed === 'cognitive-load') {
      const subjectId = data['belief-state-changes'].subject;
      let callSign;
      if (p1.uniqueid === subjectId) callSign = p1.callsign;
      else if (p2.uniqueid === subjectId) callSign = p2.callsign;
      else callSign = p3.callsign;

      setTimeout(function () {
        emit.emit('newCognitiveLoad', [data['belief-state-changes'].values, callSign]);
      }, 100);
    }
  }
};

// --- Internal functions used in handle testbed rmq log file ---
const getPlayersFlexibleRoles = (subjectIds) => {
  const players = [];
  for (let i = 0; i < subjectIds.length; i++) {
    const player = new Player('N/A', 'N/A', subjectIds[i], subjectIds[i]);
    players.push(player);
  }
  return players;
};

const getPlayersFixedRoles = (clientInfo) => {
  const players = [];
  // filter out advisor
  for (let i = 0; i < clientInfo.length; i++) {
    if (clientInfo[i].participant_id === '' || clientInfo[i].playername === 'asist_advisor') {
      continue;
    }

    const player = new Player(
      clientInfo[i].playername || clientInfo[0].participant_id,
      clientInfo[i].callsign,
      clientInfo[i].participant_id,
      clientInfo[i].unique_id
    );

    players.push(player);
  }
  return players;
};

const handleStartTrialMsg = (msg, data, header) => {
  const { name, trial_number, condition, subjects, experiment_name, experiment_mission, testbed_version, map_name } = data;
  const trial_date = data.date;

  let player, player1, player2, player3, isFixedRoles, isMultiPlayer;

  // 1. Single player: todo
  if (msg.version === 'not set' || header.version * 1 === 1.0) {
    isMultiPlayer = false;
    isFixedRoles = false;
    player = new Player('Place holder');
    emit.emit('trial_start', {
      isMultiPlayer,
      player,
      name,
      trial_number,
      trial_date,
      testbed_version,
      condition,
      subjects,
      experiment_name,
      map_name,
      experiment_mission,
    });
    return [isMultiPlayer, isFixedRoles, player];
  }

  // 2. Multi-players
  if (header.version * 1 === 0.6 || header.version * 1 === 1.1) {
    isMultiPlayer = true;
    if (!data.client_info || !data.client_info.length) {
      [player1, player2, player3] = getPlayersFlexibleRoles();
      isFixedRoles = false;
    } else {
      [player1, player2, player3] = getPlayersFixedRoles(data.client_info);
      isFixedRoles = true;
    }

    emit.emit('trial_start', {
      isMultiPlayer,
      isFixedRoles,
      player1,
      player2,
      player3,
      name,
      trial_number,
      trial_date,
      testbed_version,
      condition,
      subjects,
      experiment_name,
      map_name,
      experiment_mission,
    });
    return [isMultiPlayer, isFixedRoles, player1, player2, player3];
  }
};

function moveVictim(data, emitType) {
  const victimLocation = { x: data.victim_x, z: data.victim_z };
  const participantId = data.participant_id ? data.participant_id : data.playername;
  const victimId = data.victim_id;
  let victimType;
  if (data.color) {
    victimType = data.color.toLowerCase() === 'green' ? 'regular' : 'critical';
  } else if (data.type) {
    const victimRegular = ['victim_1', 'regular', 'victim_a', 'victim_b'];
    const victimRegularSaved = ['victim_saved_a', 'victim_saved_b'];
    const victimCriticalSaved = ['victim_saved_c'];

    if (victimRegular.includes(data.type.toLowerCase())) {
      victimType = 'regular';
    } else if (victimRegularSaved.includes(data.type.toLowerCase())) {
      victimType = 'regular_triaged';
    } else if (victimCriticalSaved.includes(data.type.toLowerCase())) {
      victimType = 'critical_triaged';
    } else {
      victimType = 'critical';
    }
  }

  emit.emit(emitType, { participantId, victimLocation, victimType, victimId });
}

function getPlayersNumber(data, players) {
  let playerNumber, playerCallSign;
  const [player1, player2, player3] = players;
  const participantId = data.participant_id ? data.participant_id : data.playername;
  if (participantId === player1.participantId || participantId === player1.playerName) {
    playerNumber = 'player1';
    playerCallSign = player1.callSign;
  } else if (participantId === player2.participantId || participantId === player2.playerName) {
    playerNumber = 'player2';
    playerCallSign = player2.callSign;
  } else if (participantId === player3.participantId || participantId === player3.playerName) {
    playerNumber = 'player3';
    playerCallSign = player3.callSign;
  }

  return [participantId, playerNumber, playerCallSign];
}

function getMarkerTypeAndLocation(data, msg, players) {
  const [player1, player2, player3] = players;
  let markerType;
  const markerLocation = { x: data.marker_x, z: data.marker_z };
  const [participantId, _playerNumber, playerCallSign] = getPlayersNumber(data, [player1, player2, player3]);

  if (msg.version * 1 === 2 || msg.version * 1 === 2.1) {
    markerType = data.type; // example: green_rubble
  } else if (msg.version * 1 === 0.5) {
    const blockType = data.type.trim().split(' ')[-1]; // Data format: "Marker Block 1", extract the number
    markerType = `${playerCallSign}.toLowerCase()}_${blockType}`; // ex: green_1
  }
  return [participantId, markerType, markerLocation];
}

// -------------------------
// Digesting incoming data
// -------------------------
const selectedFloor = 60;
let isMultiPlayer, isFixedRole, rest, isMissionOn, isTrialOn;
let player, player1, player2, player3;
let triagedRegular = 0;
let triagedCritical = 0;
let savedRegular = 0;
let savedCritical = 0;

const resetStats = () => {
  timeObject.minTimestamp = Infinity;
  timeObject.timeDurationTracker = Infinity;
  timeObject.previousTimestamp = 0;
  triagedRegular = 0;
  triagedCritical = 0;
  savedRegular = 0;
  savedCritical = 0;
};

// Communicate with SocketIO backend using RMQ event
emit.on('clearState', () => {
  resetStats();
});

const processMsg = async (input) => {
  const receivedMsg = await JSON.parse(input.content.toString());
  // console.log(input);
  if (receivedMsg['routing-key'] === 'observations.ui') handleRLRobot(receivedMsg);
  else if (receivedMsg['routing-key'] === 'predictions') handleRITAPredictions(receivedMsg);
  else if (receivedMsg['routing-key'] === 'belief-state-changes') handleCognitiveLoad(receivedMsg);
  else if (!receivedMsg['routing-key'] || receivedMsg['routing-key'] === 'testbed-message') {
    if (receivedMsg['testbed-message'] && receivedMsg['testbed-message'].msg) {
      const { msg, header, data } = receivedMsg['testbed-message'];

      if (!msg.timestamp) return;

      switch (header.message_type) {
        case 'trial':
          if (msg.sub_type === 'stop') {
            isTrialOn = false;
            emit.emit('trial_stop');
            return;
          }
          resetStats();
          isTrialOn = true;
          [isMultiPlayer, isFixedRole, ...rest] = handleStartTrialMsg(msg, data, header);
          if (isMultiPlayer) {
            [player1, player2, player3] = rest;
          } else {
            [player] = rest;
          }
          console.log(`Trial On: ${isTrialOn}`);
          break;

        case 'groundtruth':
          if (msg.sub_type === 'Mission:VictimList') {
            emit.emit('victim_list', data.mission_victim_list);
          } else if (msg.sub_type === 'Mission:BlockageList') {
            emit.emit('blockage_list', data.mission_blockage_list);
          } else if (msg.sub_type === 'Mission:FreezeBlockList') {
            emit.emit('freezeBlock_list', data.mission_freezeblock_list);
          } else if (msg.sub_type === 'Mission:ThreatSignList') {
            emit.emit('threatsign_list', data.mission_threatsign_list);
          } else if (msg.sub_type === 'Event:VictimsRescued') {
            // From a very old data set
            emit.emit('victims_rescued', data.rescued_message);
          } else if (msg.sub_type === 'Mission:RoleText') {
            // todo: puzzle text info
          }
          // Victimes expired (Study 1 or older)
          else if (msg.sub_type === 'Event:VictimsExpired') {
            emit.emit('victims_timeout', data.expired_message);
          }
          break;
        case 'event':
          switch (msg.sub_type) {
            case 'Event:MissionState':
              if (data.mission_state === 'Start') {
                isMissionOn = true;
                emit.emit('mission_start');
              }
              if (data.mission_state === 'Stop') {
                isMissionOn = false;
                emit.emit('mission_stop');
              }
              console.log(`Mission On: ${isMissionOn}`);
              break;
            case 'Event:Pause':
              emit.emit('pause', { pause_status: data.paused });
              break;
            case 'Event:Door': {
              const participantId = data.participant_id ? data.participant_id : data.playername;
              const doorEventData = receivedMsg['testbed-message'].data;
              const coordinates = { z: doorEventData.door_z, x: doorEventData.door_x };
              emit.emit('event_door', { state: doorEventData.open, coordinates: coordinates, participantId });
              break;
            }
            case 'Event:RoleSelected': {
              if (!isMultiPlayer) break;
              const [participantId, playerNumber] = getPlayersNumber(data, [player1, player2, player3]);
              const { new_role, prev_role } = data;
              emit.emit('event_roleChange', { playerNumber, participantId, new_role, prev_role });
              break;
            }
            case 'Event:Perturbation': {
              let { type, mission_state } = data;
              mission_state = mission_state.toLowerCase() === 'start' ? 'On' : 'Off';
              type = mission_state === 'On' ? type : 'None';
              emit.emit('event_perturbation', { type, state: mission_state });
              break;
            }
            case 'Event:PerturbationRubbleLocations': {
              emit.emit('blockage_list', data.mission_blockage_list);
              break;
            }
            case 'Event:PlayerFrozenStateChange': {
              const isFrozen = data.state_changed_to === 'FROZEN'; // true or false
              const playerLocation = { x: data.player_x, z: data.player_z };
              const participantId = data.participant_id ? data.participant_id : data.playername;
              const medicPlayerName = !isFrozen ? data.medic_playername : 'N/A';
              emit.emit('event_playerFrozen', { participantId, medicPlayerName, isFrozen, playerLocation });
              break;
            }
            case 'Event:VictimPickedUp': {
              moveVictim(data, 'event_victimPickedUp');
              break;
            }
            case 'Event:VictimPlaced': {
              moveVictim(data, 'event_victimPlaced');
              break;
            }
            case 'Event:Triage': {
              if (!(data.triage_state === 'SUCCESSFUL')) break; // 3 state: UNSUCCESSFUL, SUCCESSFUL, IN_PROGRESS

              const victimId = data.victim_id;
              const participantId = data.participant_id ? data.participant_id : data.playername;
              const victimLocation = { x: data.victim_x, z: data.victim_z };

              let victimType;
              if (msg.version * 1 === 0.5) {
                victimType = data.color.toLowerCase() === 'green' ? 'regular' : 'critical'; // green, yellow
              } else if (msg.version * 1 === 1.2) {
                victimType = data.type.toLowerCase; // REGULAR, CRITICAL
              } else if (msg.version * 1 === 2) {
                victimType = data.type.toLowerCase() === 'victim_c' ? 'critical' : 'regular'; //  victim_a, victim_b, victim_c
              }

              triagedRegular += victimType === 'regular' ? 1 : 0;
              triagedCritical += victimType === 'critical' ? 1 : 0;
              emit.emit('event_triage', {
                victimLocation,
                victimType,
                triagedRegular,
                triagedCritical,
                participantId,
                victimId,
              });
              break;
            }
            case 'Event:VictimEvacuated': {
              const isSaved = data.success;
              const participantId = data.participant_id ? data.participant_id : data.playername;
              if (msg.version * 1 === 2) {
                const victimType = data.type;
                if (victimType === 'victim_saved_c' && isSaved) {
                  savedCritical += 1;
                } else if (isSaved) {
                  savedRegular += 1;
                }
              }

              emit.emit('event_evacuation', { savedCritical, savedRegular, participantId });
              break;
            }
            case 'Event:MarkerPlaced': {
              const [participantId, markerType, markerLocation] = getMarkerTypeAndLocation(data, msg, [player1, player2, player3]);
              emit.emit('event_markerPlaced', { participantId, markerType, markerLocation });
              break;
            }
            case 'Event:MarkerRemoved': {
              const [participantId, markerType, markerLocation] = getMarkerTypeAndLocation(data, msg, [player1, player2, player3]);
              emit.emit('event_markerRemoved', { participantId, markerType, markerLocation });
              break;
            }

            case 'Event:RubbleCollapse': {
              const fromBlockX = data.fromBlockX || data.fromBlock_x;
              const toBlockX = data.toBlockX || data.toBlock_x;
              const fromBlockZ = data.fromBlockZ || data.fromBlock_z;
              const toBlockZ = data.toBlockZ || data.toBlock_z;
              const fromBlockY = data.fromBlockY || data.fromBlock_y;
              const toBlockY = data.toBlockY || data.toBlock_y;

              const rubbleLocations = [];

              for (let x = fromBlockX; x <= toBlockX; x++) {
                if (fromBlockZ <= toBlockZ) {
                  for (let z = fromBlockZ; z <= toBlockZ; z++) {
                    for (let y = fromBlockY; y <= toBlockY; y++) {
                      if (y === selectedFloor || y === selectedFloor + 1) rubbleLocations.push({ x: x, z: z, y: y });
                    }
                  }
                } else if (fromBlockZ > toBlockZ) {
                  for (let z = toBlockZ; z <= fromBlockZ; z++) {
                    for (let y = fromBlockY; y <= toBlockY; y++) {
                      if (y === selectedFloor || y === selectedFloor + 1) rubbleLocations.push({ x: x, z: z, y: y });
                    }
                  }
                }
              }
              emit.emit('event_rubbleCollapse', rubbleLocations);
              break;
            }
            case 'Event:RubbleDestroyed': {
              const rubbleLocation = { x: data.rubble_x, z: data.rubble_z, y: data.rubble_y };
              const participantId = data.participant_id ? data.participant_id : data.playername;
              emit.emit('event_rubbleDestroyed', { participantId, rubbleLocation });
              break;
            }
            default:
              return 0;
          }
          break;
        case 'observation': {
          if (msg.sub_type === 'Event:Scoreboard') {
            emit.emit('event_scoreBoard', data.scoreboard.TeamScore);
            break;
          }

          // Note: In old date, we might not have mission stop message.
          // Note: if (timeObject.mission_timer === '0 : 0' || timeObject.mission_timer === 'Mission Timer not initialized.') break;
          if (msg.sub_type === 'state' && isMissionOn && data.name !== 'ASU_MC') {
            // Create a time duration,
            timeObject.rawTimestamp = msg.timestamp;
            timeObject.processedTimestamp = Date.parse(timeObject.rawTimestamp);
            timeObject.minTimestamp = Math.min(timeObject.minTimestamp, timeObject.processedTimestamp);

            const missionDuration = timeObject.processedTimestamp - timeObject.minTimestamp;
            timeObject.missionDurationInSec = missionDuration * 0.001;
            timeObject.missionDurationInMin = timeObject.missionDurationInSec * 0.0166667;

            if (!isMultiPlayer) {
              player.updateCurrPosition(data.x, data.y, data.z);
            } else if (player1.playerName === data.playername) player1.updateCurrPosition(data.x, data.y, data.z);
            else if (player2.playerName === data.playername) player2.updateCurrPosition(data.x, data.y, data.z);
            else player3.updateCurrPosition(data.x, data.y, data.z);

            // b. Send player positions, mission_timer and timestampMin to Socket IO every 5 seconds
            if (timeObject.timeDurationTracker >= 0.5) {
              emit.emit('mission_timer', data.mission_timer); // For map
              emit.emit('timestampInMin', timeObject.missionDurationInMin); // For graphs
              if (!isMultiPlayer) {
                emit.emit('player_position', { player_pos: player.positions, playerInfo: player });
                player.positions = [];
              } else {
                emit.emit('player1_position', { player_pos: player1.positions, playerInfo: player1 }); // array, current_pos
                emit.emit('player2_position', { player_pos: player2.positions, playerInfo: player2 }); // array, current_pos
                emit.emit('player3_position', { player_pos: player3.positions, playerInfo: player3 }); // array, current_pos
                player1.positions = [];
                player2.positions = [];
                player3.positions = [];
              }
              timeObject.previousTimestamp = timeObject.missionDurationInSec;
              timeObject.timeDurationTracker = 0;
            }
            timeObject.timeDurationTracker = timeObject.missionDurationInSec - timeObject.previousTimestamp;
          }
          break;
        }

        default:
          return 0;
      }
    }
  }
};

module.exports.emit = emit;
module.exports.processMsg = processMsg;
