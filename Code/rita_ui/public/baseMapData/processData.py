#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Mar  9 10:02:37 2021
@author: tphuong

Note
    - Usage: 
    • cd to public/baseMapData 
    • command: python processData.py -i input.json -o output.json

    - Coordinates: 
    • Data from input json file is in form of [x, y, z] ==> We will use the key '[x, z]' to locate the object
    • normalizedData returns: an dictionary of: "[x, z]": "object_name"
    • "normalizedMaxZ" = height property in the baseMap-0x.json
    • "normalizedMaxX" = width property in the baseMap-0x.json

    - Base floor: 
    • Falcon: Base floor is 60, which is the lowest floor 
    • Saturn: Base floor is 60, which is not the lowest floor (59)

    - To create the basic map: 
    • Copy the array created from constructTileMapData (name "tileMapData") to layers.data in baseMap-0x.json
    • Open Tile application and adjust the image as needed
    • Also, in the baseMap-0x.json, adjust the width (normalizedMaxX + 1) and height (normalizedMaxZ + 1) -- If we don't add 1, we will get "corupted error..."
    - 
    # wall: 241
    # empty: 640
"""

import json
import sys, getopt

minY = float('inf')
minX = float('inf')
minXFloor =  float('inf')
minZ = float('inf')
minZFloor =  float('inf')


maxX = float('-inf')
maxXFloor =  float('-inf')
maxZ = float('-inf')
maxZFloor =  float('-inf')

baseLevelData = []
uniqueObjects = set()

# Data for mapping to images
unknownObjects = {} # All unknown objects should be assigned to image names soon

objectsToImageID = {
    'empty'                 : 640,
    'unknown'               : 1048,
    'hardened_clay'         : 333,
    'iron_block'            : 241,
    'nether_brick_fence'    : 46,
    'spruce_fence'          : 507,
    'dark_oak_fence'        : 551,
    'birch_fence'           : 508,
    'nether_brick'          : 47, # 336,
    'stained_hardened_clay' : 333,
    'quartz_block'          : 346,
    'leaves'                : 292,
    'brick_block'           : 7,
    'stained_glass'         : 809,
    'barrier'               : 35,
    'stone_slab'            : 1029,   
    'double_stone_slab'     : 5,   
    
    # Doors
    'dark_oak_door'         : 813,
    'spruce_door'           : 813,
    'wooden_door'           : 813, 
    'birch_door'            : 812,  
    'acacia_door'           : 814,   
    
    # Stairs
    'spruce_stairs'         : 514,   
    'birch_stairs'          : 515,   
    'oak_stairs'            : 513,   
    'brick_stairs'          : 431,   
    'stone_brick_stairs'    : 430,
    'wooden_slab'           : 1026,
    'dark_oak_stairs'       : 518,
    
    # Additional Objects
    'fire'                      : 91,   
    'water'                     : 216,  
    'bookshelf'                 : 51,
    'ladder'                    : 123,      # ladder in Jroom
    'cauldron'                  : 226,      # Big pot in Jroom
    'wool'                      : 170,      # blockages
    'hopper'                    : 282,      # Restrooms -- No idea
    'anvil'                     : 258,      # Restrooms -- No idea
    'end_portal_frame'          : 230,      # Restrooms (Bồn cầu)
    'gravel'                    : 209,      # Sỏi -- walkable
    'flower_pot'                : 945,
    'planks'                    : 4,        # miếng gỗ (table)
    'white_glazed_terracotta'   : 672,      # Đất nung hoa văn trắng 
    'orange_glazed_terracotta'  : 674,      # Đất nung hoa văn trắng 
    'pink_glazed_terracotta'    : 683,
    'bedrock'                   : 25,       # special blockages
    'diamond_block'             : 32,       # special blockages
    'stone'                     : 1,
    'wall_sign': 90,
    'stonebrick': 78,
    'crafting_table': 84,
    'birch_fence_gate': 532,
    'furnace': 85,
    'jukebox': 107
}
       
def readFile(fileName):
    try:
        with open(fileName, 'r') as f:
            return json.load(f).get("data")            
            
    except IOError:
        print("Could not read file:", fileName)
  
       
def writeJsonToFile(fileName, data):
    try:
        with open(fileName, 'w') as outFile:
            json.dump(data, outFile)
    except IOError:
         print("Could not write to file:", fileName)
         
def extractData(data):
    global minY, minX, minXFloor, minZ, minZFloor, maxX, maxXFloor, maxZ, maxZFloor
    
    # An Example of item: [[-2045, 62, 192], 'stained_glass_pane'] 
    # Extracts boundary data and the lowest floor (minY)
    for item in data:
        minY = min(item[0][1], minY)
        minX = min(item[0][0], minX)
        minZ = min(item[0][2], minZ)
         
        maxX = max(item[0][0], maxX)
        maxZ = max(item[0][2], maxZ)
     
    # Extracts only objects located on the base level of the map   
    for item in data:
        if (item[0][1] == 60):
            minXFloor = min(item[0][0], minXFloor)
            minZFloor = min(item[0][2], minZFloor )
            maxXFloor = max(item[0][0], maxXFloor)
            maxZFloor = max(item[0][2], maxZFloor)
            baseLevelData.append(item)
   
def normalizedCoordinate(value, minValue):
    return abs(value - minValue);
       
def normalizedData():
    result = {}
    for item in baseLevelData:
        # Convert all coordinates to be in the range between [0, ?]
        normalizedX = normalizedCoordinate(item[0][0], minXFloor)
        normalizedZ = normalizedCoordinate(item[0][2], minZFloor)
        
        # Add object to uniqueObjects set for debugging purpose
        obj = item[1]
        uniqueObjects.add(obj)
        
        if obj not in objectsToImageID:
            unknownObjects[obj] = 'unknown' 
            result[str([normalizedX, normalizedZ])] = 'unknown'
        else:
            result[str([normalizedX, normalizedZ])] = obj
           
    return result
 
def constructTileMapData(data):
    tileMapArray = []
    # z = row, x = column
    for z in range (0, data['normalizedMaxZ'] + 1):
        for x in range (0, data['normalizedMaxX'] + 1):
            key = str([x, z])
           
            if key in data['data']:         
                tileMapArray.append(objectsToImageID[data['data'][key]] + 1)
            else:
                tileMapArray.append(objectsToImageID['empty'] + 1)
    return tileMapArray
                    

def main(argv):
    inputFile = ''
    outputFile = 'defaultProcessedData.json'
    
    # Handles argv inputs 
    try:
      opts, args = getopt.getopt(argv,"hi:o:",["ifile=","ofile="])
    except getopt.GetoptError:
      print('processData.py -i <inputfile> -o <outputfile>')
      sys.exit(2)

    for opt, arg in opts:
      if opt == '-h':
         print('processData.py -i <inputfile> -o <outputfile>')
         sys.exit()
      elif opt in ("-i", "--ifile"):
         inputFile = arg
      elif opt in ("-o", "--ofile"):
         outputFile = arg
    
    # Handles data     
    if(inputFile):  
        # 1. Get data from file input
        data = readFile(inputFile)
        
        # 2. Extract the baseLevelData and boundary info
        extractData(data) 
        
        # 3. Process baseLevelData to produce an array of [[normalizedX, normalizedY], 'objectImageName']
        resultData = normalizedData()  
        
        processedData = {
            'data': resultData,
            'minY': minY,
            'minX': minX,
            'maxX': maxX,
            'minZ': minZ,
            'maxZ': maxZ,
            'minXFloor': minXFloor,
            'maxXFloor': maxXFloor,
            'minZFloor': minZFloor,
            'maxZFloor': maxZFloor,
            'normalizedMaxX': normalizedCoordinate(maxXFloor,  minXFloor),
            'normalizedMaxZ': normalizedCoordinate(maxZFloor, minZFloor),
            'objectsToImageID' : objectsToImageID,
            'unknownObjects': unknownObjects
            }
       
       
        # 4. Construct tile mape data (an array of numbers that map to correct image ids from png file)
        tileMapData = constructTileMapData(processedData)
        jsonResultData = processedData.copy() 
        jsonResultData['tileMapData'] = tileMapData
       
        
        # 5. Export data to json file
        writeJsonToFile(outputFile, jsonResultData)
        
        #6. Return
        return jsonResultData;
                    
if __name__ == "__main__":
   main(sys.argv[1:])
   
   

   
   
   
   
   
   
   
   
   
   
