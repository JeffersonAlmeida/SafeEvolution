'''
Created on Jun 5, 2013

@author: Jefferson Almeida
'''

import os
import random
import sys
os.chdir("/media/jefferson/Expansion Drive/targetWorkspace/TaRGeT")
c = 1
def log(revision):
    f = os.popen('svn log trunk/ --incremental -r %s' % revision) # svn log --incremental -r revision
    output = f.read()
    print(output)
    return output

def alrealdy_used(revision, used):
    used.close;
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
    print('manipulating directories')
    f = os.popen('ls -l')
    print (f.read())
    
    f = os.popen('mkdir src/')
    print (f.read())
    
    f = os.popen('ls -l')
    print (f.read())
    
    f = os.popen('rm -rf TaRGeT\ PV/')
    print (f.read())
    
    f = os.popen('ls -l')
    print (f.read())
    
    f = os.popen('mv * src/')
    print (f.read())
    
    os.chdir(d + "/src/")
    f = os.popen('ls -l')
    print (f.read()) 

def find_revisions(c,f, used):
    revision = random.randint(2161,3877) 
    if not alrealdy_used(revision,used):
        if log(revision) == "":
            find_revisions(c,f,used)
        elif log(revision+1) == "":
            find_revisions(c,f,used)
        else:
            cmd = os.popen('svn copy -r %s' %revision  + ' trunk/ branches/branch%s' %c + '.0')
            output = cmd.read()
            print(output)
            manipulate_directories('branches/branch%s' %c + '.0')
            os.chdir("/media/jefferson/Expansion Drive/targetWorkspace/TaRGeT")
            svn2 = os.popen('svn copy -r %s' %(revision+1)  + ' trunk/ branches/branch%s' %c + '.1')
            out2 = svn2.read()
            print(out2)
            manipulate_directories('branches/branch%s' %c + '.1')
            f.write('svn copy -r %s' %revision  + ' trunk/ branches/branch%s' %c + '.0' + '\n')
            f.write('svn copy -r %s' %(revision+1)  + ' trunk/ branches/branch%s' %c + '.1' + '\n')
            f.write('\n')
            used.write('%s' %revision + '\n')
    else:
        find_revisions(c,f,used)
        

def create_branches():
    array = sys.argv
    numberOfEvolutionPairs = array[1]
    branchNumber = array[2]
    i = int(branchNumber)
    size = i + int(numberOfEvolutionPairs)
    f = open('logFile','a')
    used = open('used','a')
    while (i<size):
        find_revisions(i,f,used)
        i = i+1
    f.close()
    used.close()  
     
create_branches()     

