module.exports = class Player {
  constructor(playerName, callSign, participantId, uniqueId) {
    this.playerName = playerName;
    this.callSign = callSign;
    this.participantId = participantId;
    this.uniqueId = uniqueId;

    this.pos_x = null;
    this.pos_z = null;
    this.pos_y = null;
    this.positions = [];
    this.triagedReg = 0;
    this.triagedCrt = 0;
    this.savedReg = 0;
    this.savedCrt = 0;
  }

  // Method
  updateCurrPosition(x, y, z) {
    this.pos_x = x;
    this.pos_y = y;
    this.pos_z = z;
    if (this.positions[this.positions.length - 1] === [this.pos_x, this.pos_z]) return;
    this.positions.push([this.pos_x, this.pos_z]);
  }

  resetPositions() {
    this.position = [];
  }
};
