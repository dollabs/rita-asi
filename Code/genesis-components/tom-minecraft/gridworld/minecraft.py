import random
import datetime
import csv
from os.path import join

TIME_LIMIT = 360000
MAP = '24by24_6.csv' #'36by64_40.csv'  #
def SEED(): return str(random.randint(0, 100))
WORLD_STRING = '3;7,220*1,5*3,2;'+SEED()+';,biome_1'
RECORDING_FOLDER = 'recordingsMalmo'


def get_mission_xml(MAP=MAP, OBS_MODE=False):

    blocks = {}
    draw_blocks = ''
    draw_doors = ''
    draw_signs = ''

    with open(join('maps',MAP), encoding='utf-8-sig') as csv_file:
        csv_reader = csv.reader(csv_file, delimiter=',')
        world_grids = []
        for row in csv_reader:
            world_grids.append(row)
        world_height = len(world_grids)
        world_width = len(world_grids[0])
    
    glass = 'glass'
    wall = 'iron_block'
    door = 'dark_oak_door'
    gravel = 'dirt'
    victim = 'chest'
    victim_yellow = 'ender_chest'

    ## Stata blocks
    wall_lower = 'planks' #brick_block'
    grass = 'tnt'
    grass_bottom = 'grass'
    entrance = 'red_mushroom_block'
    entrance_top = 'red_flower'
    hand_sanitizer_upper = 'stone_button'
    hand_sanitizer_body = 'dispenser'
    hand_sanitizer_lower = 'iron_bars'
    bench = 'birch_stairs'
    table = 'enchanting_table'
    elevator_door = 'iron_door'

    base_y = 226
    start_y = base_y + 1
    for i in range(-1, world_height+1):
        for j in range(-1, world_width+1):
            # if '36by64' not in MAP: 
            if not OBS_MODE: blocks[(j,base_y+5,i)] = glass

            if i > -1 and i < world_height and j > -1 and j < world_width:
                blocks[(j,base_y,i)] = wall
                grid = world_grids[i][j]

                if grid in ['S','0','90','180','270']:
                    start_x = j
                    start_z = i
                if grid == 'S' or grid == '0':
                    start_heading = 0
                if grid == '90':
                    start_heading = 90
                if grid == '180':
                    start_heading = 180
                if grid == '270':
                    start_heading = -90

                ## DARPA experiment blocks
                if grid == 'V':
                    if not OBS_MODE:
                        blocks[(j,base_y+1,i)] = victim
                    else:
                        blocks[(j,base_y+2,i)] = victim
                if grid == 'VV':
                    if not OBS_MODE:
                        blocks[(j,base_y+1,i)] = victim-yellow
                    else:
                        blocks[(j,base_y+2,i)] = victim-yellow
                if grid == 'W':
                    blocks[(j,base_y+1,i)] = wall_lower
                    blocks[(j,base_y+2,i)] = wall_lower
                    blocks[(j,base_y+3,i)] = wall
                    if not OBS_MODE: blocks[(j,base_y+4,i)] = wall
                if grid == 'D':
                    if world_grids[i+1][j] == 'W' and world_grids[i-1][j] == 'W':
                        draw_doors += '<DrawLine x1="'+str(j)+'" y1="'+str(base_y+1)+'" z1="'+str(i)+'" x2="'+str(j)+'" y2="'+str(base_y+2)+'" z2="'+str(i)+'" type="' +door+ '" face="EAST"/>'
                    elif world_grids[i][j+1] == 'W' and world_grids[i][j-1] == 'W':
                        draw_doors += '<DrawLine x1="'+str(j)+'" y1="'+str(base_y+1)+'" z1="'+str(i)+'" x2="'+str(j)+'" y2="'+str(base_y+2)+'" z2="'+str(i)+'" type="' +door+ '" face="SOUTH"/>'
                if grid == 'G':
                    if not OBS_MODE:
                        blocks[(j,base_y+1,i)] = gravel
                    blocks[(j,base_y+2,i)] = gravel
                # if grid == 'F':
                #     blocks[(j,base_y+1,i)] = 'fire'


                ## Stata blocks
                if grid == 'H':
                    if not OBS_MODE:
                        blocks[(j,base_y+1,i)] = hand_sanitizer_lower
                        blocks[(j,base_y+2,i)] = hand_sanitizer_body
                    else:
                        blocks[(j,base_y+2,i)] = hand_sanitizer_body
                if grid == 'B':
                    blocks[(j,base_y+1,i)] = bench
                if grid == 'T':
                    blocks[(j,base_y+1,i)] = table
                if grid == 'O':
                    if not OBS_MODE: 
                        blocks[(j,base_y+1,i)] = grass
                        blocks[(j,base_y+2,i)] = grass
                        blocks[(j,base_y+3,i)] = grass
                    blocks[(j,base_y,i)] = grass_bottom
                if grid == 'E':
                    blocks[(j,base_y,i)] = grass_bottom
                    blocks[(j,base_y+1,i)] = entrance
                    blocks[(j,base_y+1,i)] = entrance
                    blocks[(j,base_y+2,i)] = entrance
                    blocks[(j,base_y+3,i)] = entrance
                    if not OBS_MODE: blocks[(j,base_y+4,i)] = entrance_top
                if grid == 'I':
                    blocks[(j,base_y+1,i)] = wall
                    blocks[(j,base_y+2,i)] = wall
                    blocks[(j,base_y+3,i)] = wall
                    if not OBS_MODE: blocks[(j,base_y+4,i)] = wall
                if grid == 'L':
                    if world_grids[i+1][j] == 'I' and world_grids[i-1][j] == 'I':
                        draw_doors += '<DrawLine x1="'+str(j)+'" y1="'+str(base_y+1)+'" z1="'+str(i)+'" x2="'+str(j)+'" y2="'+str(base_y+2)+'" z2="'+str(i)+'" type="' +elevator_door+ '" face="EAST"/>'
                    elif world_grids[i][j+1] == 'I' and world_grids[i][j-1] == 'I':
                        draw_doors += '<DrawLine x1="'+str(j)+'" y1="'+str(base_y+1)+'" z1="'+str(i)+'" x2="'+str(j)+'" y2="'+str(base_y+2)+'" z2="'+str(i)+'" type="' +elevator_door+ '" face="SOUTH"/>'
                    blocks[(j,base_y+3,i)] = wall
                    if not OBS_MODE: blocks[(j,base_y+4,i)] = wall
                if ':' in grid:
                    if world_grids[i+1][j] == 'W':
                        draw_signs += '<DrawSign x="{}" y="{}" z="{}" type="wall_sign" line1="{}" line2="{}" line3="{}" line4="{}" face="NORTH"/>'.format(str(j), str(base_y+2), str(i), '', grid[1:], '', '')
                    if world_grids[i-1][j] == 'W':
                        draw_signs += '<DrawSign x="{}" y="{}" z="{}" type="wall_sign" line1="{}" line2="{}" line3="{}" line4="{}" face="SOUTH"/>'.format(str(j), str(base_y+2), str(i), '', grid[1:], '', '')

            ## don't give a outline if using stats maze
            elif '36by64' not in MAP:
                blocks[(j,base_y,i)] = wall
                blocks[(j,base_y+1,i)] = wall_lower
                blocks[(j,base_y+2,i)] = wall_lower
                blocks[(j,base_y+3,i)] = wall
                if not OBS_MODE: blocks[(j,base_y+4,i)] = wall
    
    ## draw blocks (except for doors) on map
    for block in blocks: 
        x,y,z = block
        draw_blocks += '<DrawBlock x="'+str(x)+'" y="'+str(y)+'" z="'+str(z)+'" type="' +blocks[block]+ '"/>'

    return '''
    <?xml version="1.0" encoding="UTF-8" standalone="no" ?>
    <Mission xmlns="http://ProjectMalmo.microsoft.com" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

        <About>
            <Summary>Example 24 by 24 maze for human search and rescue experiments.</Summary>
        </About>

        <ServerSection>
            <ServerInitialConditions>
                <Time><StartTime>1</StartTime></Time>
            </ServerInitialConditions>
            <ServerHandlers>
                <FlatWorldGenerator generatorString="'''+WORLD_STRING+'''"/>
                <DrawingDecorator>''' +draw_blocks+draw_doors+draw_signs+'''
                </DrawingDecorator>
                <ServerQuitFromTimeUp timeLimitMs="'''+str(TIME_LIMIT)+'''"/>
                <ServerQuitWhenAnyAgentFinishes/>
            </ServerHandlers>
        </ServerSection>

        <AgentSection mode="Survival">
            <Name>Player</Name>
            <AgentStart>
                <Placement x="''' + str(start_x + 0.5) + '''" y="''' + str(start_y + 0.5) + '''" z="''' + str(start_z + 0.5) + '''" pitch="''' + str(start_heading) + '''" yaw="0"/>
                <Inventory>
                    <InventoryItem type="stone_pickaxe" slot="0"/>
                </Inventory>
            </AgentStart>
            <AgentHandlers>
                <DiscreteMovementCommands/>

                <ObservationFromFullStats/>
                <ObservationFromDiscreteCell/>
                <ObservationFromRay includeNBT="true"/>
                <ObservationFromHotBar/>
                <ObservationFromGrid>
                    <Grid name="nearby">
                        <min x="-1" y="-1" z="-1"/>
                        <max x="1" y="-1" z="1"/>
                    </Grid>
                </ObservationFromGrid>
                <!--VideoProducer><Width>860</Width><Height>480</Height></VideoProducer-->

            </AgentHandlers>
        </AgentSection>

    </Mission>
    ''', blocks, (start_x + 0.5, start_y + 0.5, start_z + 0.5) 
# <BuildBattleDecorator>  <DrawingDecorator>

def get_recordings_directory():
    return join(RECORDING_FOLDER, MAP + "_" + get_time() + ".tgz")

def get_time():
    return datetime.datetime.now().strftime("%m-%d-%H-%M-%S")

# {'air'|'stone'|'grass'|'dirt'|'cobblestone'|'planks'|'sapling'|'bedrock'|'flowing_water'|'water'|'flowing_lava'|'lava'|'sand'|'gravel'|'gold_ore'|'iron_ore'|'coal_ore'|'log'|'leaves'|'sponge'|'glass'|'lapis_ore'|'lapis_block'|'dispenser'|'sandstone'|'noteblock'|'bed'|'golden_rail'|'detector_rail'|'sticky_piston'|'web'|'tallgrass'|'deadbush'|'piston'|'piston_head'|'wool'|'piston_extension'|'yellow_flower'|'red_flower'|'brown_mushroom'|'red_mushroom'|'gold_block'|'iron_block'|'double_stone_slab'|'stone_slab'|'brick_block'|'tnt'|'bookshelf'|'mossy_cobblestone'|'obsidian'|'torch'|'fire'|'mob_spawner'|'oak_stairs'|'chest'|'redstone_wire'|'diamond_ore'|'diamond_block'|'crafting_table'|'wheat'|'farmland'|'furnace'|'lit_furnace'|'standing_sign'|'wooden_door'|'ladder'|'rail'|'stone_stairs'|'wall_sign'|'lever'|'stone_pressure_plate'|'iron_door'|'wooden_pressure_plate'|'redstone_ore'|'lit_redstone_ore'|'unlit_redstone_torch'|'redstone_torch'|'stone_button'|'snow_layer'|'ice'|'snow'|'cactus'|'clay'|'reeds'|'jukebox'|'fence'|'pumpkin'|'netherrack'|'soul_sand'|'glowstone'|'portal'|'lit_pumpkin'|'cake'|'unpowered_repeater'|'powered_repeater'|'stained_glass'|'trapdoor'|'monster_egg'|'stonebrick'|'brown_mushroom_block'|'red_mushroom_block'|'iron_bars'|'glass_pane'|'melon_block'|'pumpkin_stem'|'melon_stem'|'vine'|'fence_gate'|'brick_stairs'|'stone_brick_stairs'|'mycelium'|'waterlily'|'nether_brick'|'nether_brick_fence'|'nether_brick_stairs'|'nether_wart'|'enchanting_table'|'brewing_stand'|'cauldron'|'end_portal'|'end_portal_frame'|'end_stone'|'dragon_egg'|'redstone_lamp'|'lit_redstone_lamp'|'double_wooden_slab'|'wooden_slab'|'cocoa'|'sandstone_stairs'|'emerald_ore'|'ender_chest'|'tripwire_hook'|'tripwire'|'emerald_block'|'spruce_stairs'|'birch_stairs'|'jungle_stairs'|'command_block'|'beacon'|'cobblestone_wall'|'flower_pot'|'carrots'|'potatoes'|'wooden_button'|'skull'|'anvil'|'trapped_chest'|'light_weighted_pressure_plate'|'heavy_weighted_pressure_plate'|'unpowered_comparator'|'powered_comparator'|'daylight_detector'|'redstone_block'|'quartz_ore'|'hopper'|'quartz_block'|'quartz_stairs'|'activator_rail'|'dropper'|'stained_hardened_clay'|'stained_glass_pane'|'leaves2'|'log2'|'acacia_stairs'|'dark_oak_stairs'|'slime'|'barrier'|'iron_trapdoor'|'prismarine'|'sea_lantern'|'hay_block'|'carpet'|'hardened_clay'|'coal_block'|'packed_ice'|'double_plant'|'standing_banner'|'wall_banner'|'daylight_detector_inverted'|'red_sandstone'|'red_sandstone_stairs'|'double_stone_slab2'|'stone_slab2'|'spruce_fence_gate'|'birch_fence_gate'|'jungle_fence_gate'|'dark_oak_fence_gate'|'acacia_fence_gate'|'spruce_fence'|'birch_fence'|'jungle_fence'|'dark_oak_fence'|'acacia_fence'|'spruce_door'|'birch_door'|'jungle_door'|'acacia_door'|'dark_oak_door'}
