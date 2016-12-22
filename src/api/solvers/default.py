import itertools
from pymongo import MongoClient
from random import randint

#SETTINGS
connection = MongoClient('localhost', 27017)
db = connection.testdb

class Solver:

    def __init__(self, rtkid):
        #mandatory
        self.rtkid = rtkid
        self.rtk = db['rtks'].find_one({'id':rtkid})
        self.custom = {}
        print("RTK",self.rtk)

        self.list = self.rtk['elements']
        self.orderedlist = self.rtk['orderedlist']
        self.orderprogress = self.rtk["orderprogress"]
        try:
            self.custom["votescount"] = self.rtk['custom']['votescount']
        except:
            self.custom["votescount"] = {n:0 for n in self.list}
        try:
            self.custom["choicesmade"] = self.rtk['custom']['choicesmade']
        except:
            self.custom["choicesmade"] = {n:[] for n in self.list}

        if self.orderprogress > 0.0:
            self.candidatepairs = self.rtk["candidatepairs"]
            self.origcandidatepairs = self.rtk["origcandidatepairs"]
        else:
            self.generatecandidatepairs()

        print(self)

    def __repr__(self):
        return str(self.orderedlist)

    def generatecandidatepairs(self):
        print("generating candidate pairs")
        candidates = list(itertools.combinations(self.list,2))
        self.candidatepairs = candidates
        self.origcandidatepairs = float(len(candidates))


    def removecandidatepair(self,pair):
        try:
            self.candidatepairs.remove([pair[0],pair[1]])
        except:
            self.candidatepairs.remove([pair[1],pair[0]])

    def newpair(self):
        try:
            randompos = randint(0,len(self.candidatepairs)-1)
            pair = self.candidatepairs[randompos]
            return pair
        except:
            return (-1,-1)

    def pickchoice(self, winner, loser):
        if winner == -1:
            return 1.0
        currvotes = self.custom["votescount"][winner]
        self.custom["choicesmade"][winner].append(loser)
        self.custom["votescount"].update({winner:currvotes+1})
        self.orderedlist = [(w,self.custom["votescount"][w]) for w in sorted(self.custom["votescount"], key=self.custom["votescount"].get, reverse=True)]
        for i in range(len(self.orderedlist)):
            try:
                n = self.orderedlist[i]
                m = self.orderedlist[i+1]
                if n[1] == m[1]:
                    if n[0] in self.custom["choicesmade"][m[0]]: #means m lost against n
                        n = self.orderedlist[i]
                        m = self.orderedlist[i+1]
                        self.orderedlist[i+1] = n
                        self.orderedlist[i] = m
            except:
                pass
        self.removecandidatepair((winner,loser))
        self.orderprogress = (self.origcandidatepairs-len(self.candidatepairs))/self.origcandidatepairs
        return self.orderprogress

    def getprogress(self):
        return self.orderprogress

    def getorderedlist(self):
        return self.orderedlist

    def save(self):
        pythonobject = {"candidatepairs":self.candidatepairs,"origcandidatepairs":self.origcandidatepairs,
        "orderedlist":self.orderedlist,"orderprogress":self.orderprogress,"custom":{"votescount":self.custom["votescount"],
        "choicesmade":self.custom["choicesmade"]}}
        db['rtks'].update({"id":self.rtkid}, {"$set": pythonobject},upsert=False)

if __name__=="__main__":

    rtkid = "03cd32f0-c856-11e6-bcb7-c85b765fdb27"
    s = Solver(rtkid)
    s.generatecandidatepairs()
    print("candidatepairs", s.candidatepairs)
    for i in range(3):
        i = s.newpair()
        vote = randint(0,1)
        winner = i[vote]
        loser = i[vote-1]
        print("progress",s.pickchoice(winner, loser))
    print("orderedlist",s.getorderedlist())
    print("choicesmade",s.custom["choicesmade"])
    s.save()
