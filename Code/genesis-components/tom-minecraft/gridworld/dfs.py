import sys
import os.path #used to check if file exists
import time
import csv
import random

DEBUG = False

class Matrix:

    #takes in a filepath and creates the self.matrix from the text file
    def __init__(self,filePath, start, end):

        matrix = []
        if 'txt' in filePath:
            for line in open(filePath):
                temp = []
                for c in line:
                    temp.append(c)
                matrix.append(temp)
            matrix[1][13] = ''
            matrix[3][9] = 'E'

        elif 'csv' in filePath:
            matrix = list(csv.reader(open(filePath, mode='r', encoding='utf-8-sig')))

            matrix[start[0]][start[1]] = 'SS'
            matrix[end[0]][end[1]] = 'E'
            matrix_n = []
            line = []
            for j in range(len(matrix[0])):
                line.append('#')
            matrix_n.append(line)
            for i in range(len(matrix)):
                line2 = ['#']
                for j in range(len(matrix[i])):
                    if matrix[i][j] == 'W': line2.append('#')
                    # elif matrix[i][j] == 'S' or matrix[i][j] == '0' or matrix[i][j] == '90' or matrix[i][j] == '180'or matrix[i][j] == '270':
                    #     line2.append('S')
                    elif matrix[i][j] == 'E':
                        line2.append('E')
                    elif matrix[i][j] == 'SS':
                        line2.append('S')
                    else:
                        line2.append(' ')
                line2.append('#')
                matrix_n.append(line2)
            matrix_n.append(line)
            matrix = matrix_n

        # df = pd.DataFrame(matrix)
        # print(df)

        self.matrix = matrix
        self.getPoints()
        self.parentMap = {} #used to hold parents and the number of steps from the start
        self.sPath = [] #holds the shortest path
        self.countTime = 0 #number of iterations of the dfs


    #prints the matrix given to it on the screen
    def printMatrix(self):
        for array in self.matrix:
            to_print = ""
            for elem in array:
                to_print += elem
            print(to_print)


    #gets the start and end points from the matrix, if they don't exist
    #if will exit the program
    def getPoints(self):
        self.start = "%"
        self.end = "%"
        for indm, array in enumerate(self.matrix):
            for inda, elem in enumerate(array):
                if elem == "S":
                    self.start = (indm,inda)
                    if DEBUG: print("Start = ",self.start)
                if elem == "E":
                    self.end = (indm,inda)
                    if DEBUG: print("End = ",self.end)

        #check if start and end exists in the matrix
        if self.start == "%" or self.end == "%":
            print("Either Start or End was not in maze")
            # sys.exit()


    #dfs using a stack to find paths
    def dfs(self):
        #set up the stack and dfs
        stack = []
        visited = []
        stack.append(self.start)
        self.parentMap[self.start] = ["start",0]
        while stack:
            parent = stack.pop()
            if parent in visited:
                continue
            visited.append(parent)
            children = self.getChildren(parent)
            for child in children:
                stack.append(child)
                #checks if there is already a parent to replace if step count is lower
                if child in self.parentMap and self.parentMap[parent][1]+1 > self.parentMap[child][1]:
                    continue
                else:
                    self.parentMap[child] = [parent,self.parentMap[parent][1]+1]
                    stack.append(parent)

        self.countTime = self.countTime + 1
        #check if there is no path from the start to the end
        if self.end not in self.parentMap:
            print("There is not path to the End")
            sys.exit()
        self.returnPath()
        #call the
        if(self.countTime < 4):
            self.dfs()




    #used to get the connect children of the parent passed in
    #used try statments to catch out of index exceptions and treats them as walls
    def getChildren(self,parent):
        #have the differnt coords of the parent
        indm = parent[0]
        inda = parent[1]
        children = []
        funcList = []

            #checks to the top of the parent
        def checkTop():
            try:
                if self.matrix[indm+1][inda] == " " or self.matrix[indm+1][inda] == "E":
                    children.append((indm+1,inda))
            except IndexError:
                pass

        def checkBottom():
            try:
                if self.matrix[indm-1][inda] == " " or self.matrix[indm-1][inda] == "E":
                    children.append((indm-1,inda))
            except IndexError:
                pass

        #checks to the left of the parent
        def checkLeft():
            try:
                if self.matrix[indm][inda+1] == " " or self.matrix[indm][inda+1] == "E":
                    children.append((indm,inda+1))
            except IndexError:
                pass

        #checks to the right of the parent
        def checkRight():
            try:
                if self.matrix[indm][inda-1] == " " or self.matrix[indm][inda-1] == "E":
                    children.append((indm,inda-1))
            except IndexError:
                pass

        #make lists for the differnt possible func call orders
        funcOption1 = [checkBottom,checkTop,checkRight,checkLeft]
        funcOption2 = [checkLeft,checkBottom,checkTop,checkRight]
        funcOption3 = [checkRight,checkLeft,checkBottom,checkTop]
        funcOption4 = [checkTop,checkRight,checkLeft,checkBottom]

        #create a master list of all funcs order possiblities
        funcList = [funcOption1,funcOption2,funcOption3,funcOption4]
        random.shuffle(funcList)

        #depending on the cycle, call the different func orders
        for f in funcList[self.countTime]:
            f()

        #return the children
        return children

    #returns the shorts path from the end
    def returnPath(self):
        curr = self.end
        temp = []
        while curr != "start":
            temp.append(curr)
            curr = self.parentMap[curr][0]

        #if first time through set shortest path to temp
        #else check if temp is the shortest path and replace
        if(self.countTime == 1):
            self.sPath = temp[:]
        elif(len(temp) < len(self.sPath)):
            self.sPath = temp[:]


    #puts the paths on the matrix to display to the user
    def showPath(self):

        if DEBUG:
            print()
            print("Shortest path step count is ",len(self.sPath)-1)

        for steps in self.sPath:
            self.matrix[steps[0]][steps[1]] = bcolors.OKGREEN+"+"+bcolors.ENDC
        self.matrix[self.start[0]][self.start[1]] = "S"
        self.matrix[self.end[0]][self.end[1]] = "E"
        self.printMatrix()


#class to handle the colors
class bcolors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'

def path2actions(path, head):
    path.reverse()
    if DEBUG: print(path)
    if DEBUG: print()
    actions = []
    states = [(path[0],head)]
    for i in range(1,len(path)):
        s_p = path[i-1]
        s = path[i]

        ## find the goal heading position
        if s_p[1] == s[1] - 1:
            head_g = 0
        elif s_p[0] == s[0] + 1:
            head_g = 90
        elif s_p[1] == s[1] + 1:
            head_g = 180
        elif s_p[0] == s[0] - 1:
            head_g = 270

        if head - head_g == -90 or head - head_g == 270:
            actions.append('turn_left')
            states.append((s_p,head_g))
        elif head - head_g == 90 or head - head_g == -270:
            actions.append('turn_right')
            states.append((s_p,head_g))
        elif head - head_g == 180 or head - head_g == -180:
            actions.append('turn_left')
            states.append((s_p,head_g))
            actions.append('turn_left')
            states.append((s,(head_g+head)/2))

        actions.append('go_straight')
        states.append((s,head_g))

        head = head_g

    if DEBUG: print(actions)
    if DEBUG: print()
    return states, actions

def dfs(file, start=((12,1),0), end=(1,1)):
    """ input: pos, heading of start state; pos of end state
        output: a sequence of actions
    """
    if not os.path.isfile(file):
        file = os.path.join('maps', file)
    m = Matrix(file, start[0], end)
    m.dfs()
    if DEBUG: m.showPath()
    states, actions = path2actions(m.sPath, start[1])
    return states, actions

if __name__ == '__main__':
    #used to find the execution time of the program
    start_time = time.time()

    try:
        file = sys.argv[1]
    except IndexError:
        file = os.path.join('algorithms','dfs','testMaze.txt')
        file = os.path.join('maps','13by13_6.csv')

    actions = dfs(file, end=(1,1))

    if DEBUG: print("Execution Time = ",time.time() - start_time)
