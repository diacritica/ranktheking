import re, json
import uuid
import importlib
from bottle import request, response, abort
from bottle import post, get, put, delete
from pymongo import MongoClient
from bson.json_util import dumps

REGISTEREDSOLVERTYPES = ["DEFAULT", "ACTIVELEARNER"]

namepattern = re.compile(r'^[a-zA-Z\d]{1,64}$')
uuidpattern = re.compile(r'^[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}$')

connection = MongoClient('localhost', 27017)
db = connection.testdb


def allow_cors(func):
    """ this is a decorator which enable CORS for specified endpoint """
    def wrapper(*args, **kwargs):
        response.headers['Access-Control-Allow-Origin'] = '*' # * in case you want to be accessed via any website
        response.headers['Access-Control-Allow-Headers'] = 'Content-Type'
        response.headers['Access-Control-Allow-Methods'] = 'PUT, POST, GET, DELETE, OPTIONS'
        return func(*args, **kwargs)

    return wrapper

def generateid():
    return str(uuid.uuid1())

@post('/lists')
@allow_cors
def creation_handler():
    '''Handles list creation'''
    try:
        # parse input data
        # try:
        data = request.json
        # except:
        #     raise ValueError
        print(data)
        if data is None:
            raise ValueError

        # extract and validate name
        try:
            if namepattern.match(data['name']) is None:
                raise ValueError
            if data['elements'] is None:
                raise ValueError
        except (TypeError, KeyError):
            raise ValueError

        # check for existence

    except ValueError as e:

        print("Value error", e)
        # if bad request data, return 400 Bad Request
        response.status = 400

        return

    except KeyError:
        # if name already exists, return 409 Conflict
        response.status = 409
        return

    # add name
    data.update({"id":generateid()})
    db.lists.insert(data)

    # return 200 Success
    response.headers['Content-Type'] = 'application/json'
    return json.dumps(data["id"])

@get('/lists')
@allow_cors
def listing_handler():
    '''Handles list listing'''

    response.headers['Content-Type'] = 'application/json'
    response.headers['Cache-Control'] = 'no-cache'
    return dumps(db.lists.find({},{"_id":0,"id":1,"name":1}))
    #return json.dumps({'lists': list(_lists)})

@get('/lists/<alist>')
@allow_cors
def detail_handler(alist):
    '''Handles a list detail'''

    response.headers['Content-Type'] = 'application/json'
    response.headers['Cache-Control'] = 'no-cache'

    entity = db.lists.find_one({'id':alist},{"_id":0})
    if not entity:
        abort(404, 'No document with id %s' % id)
    return json.dumps(entity)


@delete('/lists/<alist>')
@allow_cors
def delete_handler(alist):
    '''Handles name updates'''
    response.headers['Content-Type'] = 'application/json'
    response.headers['Cache-Control'] = 'no-cache'

    entity = db.lists.find_one({'id':alist})
    if not entity:
        abort(404, 'No document with id %s' % id)
    db.lists.remove(entity)
    response.status = 200
    return

@get('/rtks')
@allow_cors
def rtklisting_handler():
    '''Handles RTK listing'''

    response.headers['Content-Type'] = 'application/json'
    response.headers['Cache-Control'] = 'no-cache'
    return dumps(db.rtks.find({},{"id":1,"name":1,"_id":0}))
    #return json.dumps({'lists': list(_lists)})

@get('/solvers')
@allow_cors
def solverlisting_handler():
    '''Handles solver listing'''

    response.headers['Content-Type'] = 'application/json'
    response.headers['Cache-Control'] = 'no-cache'
    return dumps(db.solvers.find({},{"id":1,"name":1,"_id":0}))
    #return json.dumps({'lists': list(_lists)})


@post('/rtks')
@allow_cors
def creation_handler():
    '''Handles rtks creation'''
    try:
        # parse input data
        # try:
        data = request.json
        # except:
        #     raise ValueError
        if data is None:
            raise ValueError
        # extract and validate name
        try:
            if uuidpattern.match(data['listid']) is None:
                raise ValueError
            if data['name'] is None:
                raise ValueError
            if data.get('solvertype') is None:
                data['solvertype'] = "DEFAULT"
            if data.get('partialocclusion') is None:
                data['partialocclusion'] = False
            # if data['solvertype'] not in REGISTEREDSOLVERTYPES:
            #     data.update({"solvertype":"DEFAULT"})
        except (TypeError, KeyError):
            raise ValueError

        # check for existence

    except ValueError as e:

        print("Value error", e)
        # if bad request data, return 400 Bad Request
        response.status = 400

        return

    except KeyError:
        # if name already exists, return 409 Conflict
        response.status = 409
        return

    # add name
    data.update({"id":generateid()})
    data.update({"orderprogress":0.0})
    data.update({"orderedlist":[]})
    l = db.lists.find_one({"id":data["listid"]})
    data.update({"elements":l['elements']})
    data.update({"custom":{}})
    data.update({"candidatepairs":[]})
    data.update({"origcandidatepairs":0})


    db.rtks.insert(data)

    # return 200 Success
    response.headers['Content-Type'] = 'application/json'
    return json.dumps(data["id"])

@get('/rtks/<artk>')
@allow_cors
def rtkdetail_handler(artk):
    '''Handles detail view of RTK'''

    response.headers['Content-Type'] = 'application/json'
    response.headers['Cache-Control'] = 'no-cache'
    entity = db.rtks.find_one({'id':artk},{"_id":0})
    if not entity:
        abort(404, 'No document with id %s' % id)
    return json.dumps(entity)

#@post('/rtks/<artk>/choice')

#parametros: idrtk, winner: string, loser: string
# return progress

@post('/rtks/<artk>/choice')
@allow_cors
def rtkchoice_handler(artk):
    '''Handles artk choice'''

    try:
        # parse input data
        # try:
        data = request.json
        # except:
        #     raise ValueError
        if data is None:
            raise ValueError
        # extract and validate name

        entity = db.rtks.find_one({'id':artk})
        if not entity:
            abort(404, 'No document with id %s' % id)
        try:
            if data.get('winner') is None:
                raise ValueError
            if data.get('loser') is None:
                raise ValueError
        except (TypeError, KeyError):
            raise ValueError


    except ValueError as e:

        print("Value error", e)
        # if bad request data, return 400 Bad Request
        response.status = 400

        return

    except KeyError:
        # if name already exists, return 409 Conflict
        response.status = 409
        return

    solvertype = entity["solvertype"]
    solver = importlib.import_module("api.solvers."+solvertype.lower())
    s = solver.Solver(artk)
    progress = s.pickchoice(data.get('winner'),data.get('loser'))
    s.save()
    # return 200 Success
    response.headers['Content-Type'] = 'application/json'
    return json.dumps(progress)


@get('/rtks/<artk>/newpair')
@allow_cors
def detail_handler(artk):
    '''Handles new pairs for a rtk'''

    response.headers['Content-Type'] = 'application/json'
    response.headers['Cache-Control'] = 'no-cache'

    entity = db.rtks.find_one({'id':artk})
    if not entity:
        abort(404, 'No document with id %s' % id)

    solvertype = "DEFAULT" #entity["solvertype"]
    solver = importlib.import_module("api.solvers."+solvertype.lower())
    s = solver.Solver(artk)
    pair = s.newpair()

    return json.dumps({"pair":pair,"progress":s.orderprogress})

@bottle.route('/<:re:.*>', method='OPTIONS')
#@bottle.route('/:#.*#', method='OPTIONS')  # Also tried old syntax.
def enableCORSGenericRoute():
    print 'Generic regex route'
    bottle.response.headers['Access-Control-Allow-Origin'] = '*'

