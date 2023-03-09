from PIL import Image, ImageSequence
from os import listdir, getcwd, mkdir
from os.path import isfile, isdir, join
import argparse

## example: python GIF_resizer.py -s 6
parser = argparse.ArgumentParser(description='Parse arguments for the code.')
parser.add_argument('-s', '--size', type=str, default='0', help='Size of the texture to generate')
parser.add_argument('-f', '--focus', type=str, default='', help='specific texture to generate')
args = parser.parse_args()
size = int(args.size)
focus = args.focus

def resize(size, CHECK=False):

    parentpath = '.'
    oldpath = '60'
    newpath = str(size)

    ## may run this script from gridworld/ instead of texture/
    if '60' not in listdir(getcwd()) and 'texture' in listdir(getcwd()):
        parentpath = 'texture'
        oldpath = join(parentpath, oldpath)
        
    if newpath not in listdir(parentpath):
        newpath = join(parentpath, newpath)
        mkdir(newpath)
    else:
        if CHECK: return
        newpath = join(parentpath, newpath)
    print(f'... creating texture of size {size}')

    gifs = [f for f in listdir(oldpath) if isfile(join(oldpath, f))]

    ## copy a resized version of each GIF to the target folder
    for file in gifs:
        if '.DS_Store' not in file and (not isfile(join(newpath, file)) or focus in file):
            im = Image.open(join(oldpath, file))

            # Get sequence iterator
            frames = ImageSequence.Iterator(im)

            # Wrap on-the-fly thumbnail generator, SIZE = (ideal_width, ideal_height)
            def thumbnails(frames, SIZE=None):
                if SIZE == None: SIZE = (size,size)
                for frame in frames:
                    thumbnail = frame.copy()
                    thumbnail.thumbnail(SIZE, Image.ANTIALIAS)
                    yield thumbnail

            if 'goals' in file:
                frames = thumbnails(frames, SIZE=(size//3, size//2))
            else:
                frames = thumbnails(frames)

            # Save output
            om = next(frames) # Handle first frame separately
            om.info = im.info # Copy sequence info
            om.save(join(newpath,file), save_all=True, append_images=list(frames))

if size == 0:
    for file in listdir(getcwd()):
        if isdir(file) and len(file) <= 2:
            size1 = int(file)
            resize(size1)
else:
    resize(size)

if __name__ == '__main__':
    for file in listdir(getcwd()):
        if isdir(file) and len(file) <= 2:
            size1 = int(file)
            resize(size1)

    # resize(9)