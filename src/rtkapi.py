import bottle
from api import lists

app = application = bottle.default_app()

if __name__ == '__main__':
    bottle.run(host = 'rtk.icarus.live', port = 8001)
