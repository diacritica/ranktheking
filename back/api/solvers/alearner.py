import itertools
from pymongo import MongoClient
from random import randint

#SETTINGS
connection = MongoClient('localhost', 27017)
db = connection.testdb

class Solver:

    '''
    Must implement
    __init__(self, rtkid)
    newpair(self)
    pickchoice(self, winner, loser)
    getprogress(self)
    getorderedlist(self)
    save(self)
    '''

    def __init__(self, rtkid):

        self.rtkid = rtkid
        self.rtk = db['rtks'].find_one({'id':rtkid})
        self.custom = {}

        self.list = self.rtk['elements']
        self.orderedlist = self.rtk['orderedlist']
        self.orderprogress = self.rtk["orderprogress"]

        try:
            self.custom["choicesmade"] = self.rtk['custom']['choicesmade']
        except:
            self.custom["choicesmade"] = {n:[] for n in self.list}

        if self.orderprogress > 0.0:
            self.candidatepairs = self.rtk["candidatepairs"]
            self.origcandidatepairs = self.rtk["origcandidatepairs"]
        else:
            self.generatecandidatepairs()

    def __repr__(self):
        return str(self.orderedlist)

    def generatecandidatepairs(self):

        tuplelistofcandidates = list(itertools.combinations(self.list,2))
        candidates = [[n[0],n[1]] for n in tuplelistofcandidates]
        self.candidatepairs = candidates
        self.origcandidatepairs = float(len(candidates))


    def removecandidatepair(self,pair):

        try:
            self.candidatepairs.remove([pair[0],pair[1]])
        except:
            try:
                self.candidatepairs.remove([pair[1],pair[0]])
            except:
                pass

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

        self.custom["choicesmade"][winner].append(loser)

        self.custom["choicesmade"][winner] = list(set(self.custom["choicesmade"][winner]+self.custom["choicesmade"][loser]))
        for i in self.custom["choicesmade"][loser]:
            self.removecandidatepair((winner,i))


        import operator
        self.removecandidatepair([winner,loser])
        self.orderedlist = [(w[0],len(w[1])) for w in self.custom["choicesmade"].items()]
        self.orderedlist.sort(key=operator.itemgetter(1), reverse=True)

        self.orderprogress = (self.origcandidatepairs-len(self.candidatepairs))/self.origcandidatepairs
        return self.orderprogress

    def getprogress(self):
        return self.orderprogress

    def getorderedlist(self):
        return self.orderedlist

    def save(self):
        pythonobject = {"candidatepairs":self.candidatepairs,"origcandidatepairs":self.origcandidatepairs,
        "orderedlist":self.orderedlist,"orderprogress":self.orderprogress,"custom":{"choicesmade":self.custom["choicesmade"]}}
        db['rtks'].update({"id":self.rtkid}, {"$set": pythonobject},upsert=False)

