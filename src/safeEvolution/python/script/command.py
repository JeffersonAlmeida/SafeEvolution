'''
Created on Jun 5, 2013

@author: Jefferson Almeida
'''

import os
import random
os.chdir("/media/jefferson/Expansion Drive/targetWorkspace/TaRGeT")
c = 1
def log(revision):
    f = os.popen('svn log trunk/ --incremental -r %s' % revision) # svn log --incremental -r revision
    output = f.read()
    print(output)
    return output

def alrealdy_used(revision, used):
    used.close;
    os.chdir("/media/jefferson/Expansion Drive/targetWorkspace/TaRGeT")
    f =  open('used','r')
    for line in f.readlines():
        r  = str(revision) + '\n';
        if line == r:
            print('equals')
            return True
    f.close()
    return False

def manipulate_directories(bn):
    d = "/media/jefferson/Expansion Drive/targetWorkspace/TaRGeT/%s" %bn
    os.chdir(d)
    os.popen('mkdir src/')
    os.popen('rm -rf TaRGeT\ PV/')
    os.popen('mv * src/')
    
def fw(c,f, used):
    while True:
        revision = random.randint(2161,3877)
        if not alrealdy_used(revision,used):
            if log(revision) != "":
                if log(revision+1) != "":
                    svn1 = os.popen('svn copy -r %s' %revision  + ' trunk/ branches/branch%s' %c + '.0/')
                    print(svn1.read())
                    revision = revision + 1
                    svn2 = os.popen('svn copy -r %s' %revision  + ' trunk/ branches/branch%s' %c + '.1/')
                    print(svn2.read())
                    manipulate_directories('branches/branch%s' %c + '.0/')
                    manipulate_directories('branches/branch%s' %c + '.1/')
                    f.write('svn copy -r %s' %revision  + ' trunk/ branches/branch%s' %c + '.0' + '\n')
                    f.write('svn copy -r %s' %(revision+1)  + ' trunk/ branches/branch%s' %c + '.1' + '\n')
                    f.write('\n')
                    used.write('%s' %revision + '\n')
                    break
        
def get_branch_number(numberOfEvolutionPairs):
    lastBranch = open('lastBranch','r')
    x = lastBranch.readline()
    print('before: %s' %x)
    lastBranch.close()
    lastBranch = open('lastBranch','w')
    y = int(x)+numberOfEvolutionPairs    
    lastBranch.write(str(y))
    print('after: %s' %y)
    lastBranch.close()       
    return y

def create_branches():
    array = sys.argv
    pairsNumber = array[1]
    i = get_branch_number(int(pairsNumber))
    size = i + int(pairsNumber)
    f = open('logFile','a')
    used = open('used','a')
    while (i<size):
        fw(i, f, used)
        i = i+1
    f.close()
    used.close()  
     
create_branches()     

