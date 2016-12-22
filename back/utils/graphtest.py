#!/usr/bin/env python

import math
import itertools
from string import ascii_uppercase
from random import randint

class RTKGraph:

    def __init__(self, alist):
        self.list = alist
        self.clist = list(itertools.combinations(alist,2))
        self.cdict = {n:(set(),set()) for n in self.list}

    def AwinsB(self, a, b):
        self.cdict[a][0].update(b)
        self.cdict[b][1].update(a)

    def getWinsForElement(self, element):
        return self.cdict[element][0]
    def getLosesForElement(self, element):
        return self.cdict[element][1]
    def getMaxWinsForElement(self):
        max = 0
        for element in self.list:
            nel = len(self.getWinsForElement(element))
            if nel > max:
                max = nel
        return max


    def propagateWins(self, awinsb):
        winner = awinsb[0]
        loser = awinsb[1]
        losers = self.getWinsForElement(winner)
        upstreamwinners = self.getLosesForElement(winner)
        for uw in upstreamwinners:
            for l in losers:
                self.AwinsB(uw,l)


    def __repr__(self):
        return(repr(self.cdict))

mylist = [i for i in ascii_uppercase[0:randint(3,len(ascii_uppercase))]]
#mylist = ["A","B","C","D","E"]

#choices = [("A","B"),("D","E"),("C","A"),("E","B"),("A","E"),("C","E"),("D","A"),("D","B"),("C","D")]


rtk = RTKGraph(mylist)
choices = []
for i in range(len(rtk.list)):
    try:
        choices.append((rtk.list[i],rtk.list[i+1]))
    except:
        pass
print("choices",choices)
n = 0
for c in choices:
    n+=1
    rtk.AwinsB(c[0],c[1])
    rtk.propagateWins(c)
    currmaxelem = rtk.getMaxWinsForElement()
    print("currmaxelem",currmaxelem)
    if currmaxelem == len(mylist)-1:
        print("\n\nlen",len(mylist),"n->",n)
        break

print(rtk)
