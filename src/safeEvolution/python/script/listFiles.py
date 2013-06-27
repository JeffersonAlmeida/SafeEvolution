'''
Created on Jun 26, 2013

@author: jefferson
'''
import os
import sys
os.chdir("/tmp")

def f():
    print (len(sys.argv))
    print  (str(sys.argv))
    array = sys.argv
    numberOfEvolutionPairs = array[1]
    branchNumber = array[2]
    print ('qtd de evolution Pairs: %s' %numberOfEvolutionPairs)
    print ('branch Name: %s ' %branchNumber)
    f= os.popen('ls -l')
    output = f.read()
    print(output)
    
f()