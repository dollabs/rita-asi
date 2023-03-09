const Phaser = require('phaser');
const Map = require('../map');

class Preloader extends Phaser.Scene {
  constructor() {
    super('preloader');
  }

  /*----- 1. Load assets before create() is called ------*/
  preload() {
    // Load the png file for base map, with key = tiles
    this.load.image('tiles_minecraft', 'tileMap/imgTileMap/minecraft_blocks.png');

    // Load JSON files (Map)
    this.load.tilemapTiledJSON('Sparky', 'tileMap/jsonTileMap/baseMap-00.json');
    this.load.tilemapTiledJSON('Falcon', 'tileMap/jsonTileMap/baseMap-01.json');
    this.load.tilemapTiledJSON('Saturn', 'tileMap/jsonTileMap/baseMap-02.json');
    this.load.tilemapTiledJSON('Saturn1.1', 'tileMap/jsonTileMap/baseMap-03.json');
    this.load.tilemapTiledJSON('Saturn1.6', 'tileMap/jsonTileMap/baseMap-04.json');
    this.load.tilemapTiledJSON('Saturn2.3', 'tileMap/jsonTileMap/baseMap-05.json'); // has the evacuation areas
    this.load.tilemapTiledJSON('Saturn2.6', 'tileMap/jsonTileMap/baseMap-06.json'); // has the evacuation areas

    // Load images of game objects
    // eslint-disable-next-line no-restricted-syntax
    for (const [key, value] of Object.entries(Map.block_to_texture)) {
      this.load.image(key, `img-map/${value}`);
    }
  }

  create() {
    this.scene.start('game');
  }
}

module.exports = Preloader;
