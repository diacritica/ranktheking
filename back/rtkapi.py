import bottle
from api import lists

app = application = bottle.default_app()

if __name__ == '__main__':
    bottle.run(host = 'localhost', port = 8001)
