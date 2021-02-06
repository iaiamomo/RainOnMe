from flask import Flask, Response
from flask_restful import Api, Resource, reqparse
import random
import string
import firebase_admin
from firebase_admin import auth
from firebase_admin import credentials
from pymongo import MongoClient

uri = 'mongodb+srv://uri'
client = MongoClient(uri)
collection = client["test"]
db = collection["leaderboards"]

app = Flask(__name__)
api = Api(app)

parseget = reqparse.RequestParser()
parseget.add_argument('req_type', type=int, required=True)
parseget.add_argument('game_id', type=str, required=None)
parseget.add_argument('who', type=str, required=True)

parserpost = reqparse.RequestParser() 
parserpost.add_argument('who', type=str, required=True)

parserput = reqparse.RequestParser()
parserput.add_argument('req_type', type=int, required=True)
parserput.add_argument('who', type=str, required=True)
parserput.add_argument('game_id', type=str, required=True)
parserput.add_argument('score', type=int, required=None)

parsecancel = reqparse.RequestParser()
parsecancel.add_argument('game_id', type=str, required=True)
parsecancel.add_argument('who', type=str, required=True)

ADD_USER = 0
SCORE = 1
USER_GAMES = 0
LEADERBOARD = 1

cred = credentials.Certificate("./serviceAccountKey.json")
firebase_admin.initialize_app(cred)

'''
{
  '_id': GAME_ID,
  '_rev': REV,
  'users': {
    'USER_ID': SCORE, ...
  }
}
'''

class Games(Resource):

  #READ
  #/?req_type=LEADERBOARD&game_id=G_ID&who=UID
  #response={'error':False, 'leaderboard':{score1: [user1, user2, ..], score2: [user3],...}, 'user_score': score}
  #/?req_type=USER_GAMES&who=UID
  #response={'error':False, 'games': [game1, game2, ...]}
  def get(self):
    args = parseget.parse_args()
    req_type = args['req_type']
    who = args['who']
    game_id = args['game_id']

    response = {'error': False}

    #USER_GAMES = 0
    if req_type == USER_GAMES:
      #get the game from DB
      try:
        res = db.find()
      except:
        response['error'] = True
        return Response(response, status=400)
        
      games = []
      res_lis = list(res)
      for i in range(len(res_lis)):
        doc = res_lis[i]
        users = doc['users']
        if who in users:
          games.append(doc['_id'])

      response['games'] = games

    #LEADERBOARD = 1
    if req_type == LEADERBOARD:
      if game_id == None: 
        response['error'] = True
        return Response(response, status=400)

      myquery = {'_id': game_id}
      what_i_want = {'users': 1}

      #get the game from DB
      try:
        res = db.find(myquery, what_i_want)
      except:
        response['error'] = True
        return Response(response, status=400)

      users = list(res)[0]['users']

      leaderboard = generate_leaderboard(users)
      user_score = users[who]

      response['leaderboard'] = leaderboard
      response['user_score'] = user_score

    return response

  #CREATE
  #/?who=UID
  #response={'error':False, 'game_id':GAME_ID}
  def post(self):
    args = parserpost.parse_args()
    who = args['who']

    response = {'error': False}

    game_id = generate_string()
    game_json = {'_id': game_id, 'users': {who: 0}}

    try:
      db.insert_one(game_json)
    except Exception as e:
      print(e)
      response['error'] = True
      return Response(response, status=400) 

    response['game_id'] = game_id
    return response
  
  #UPDATE
  #/?req_type=ADD_USER&who=UID&game_id=G_ID
  #/?req_type=SCORE&who=UID&game_id=G_ID&score=SCORE
  #response={'error':False}
  def put(self):
    args = parserput.parse_args()
    req_type = args['req_type']
    who = args['who']
    game_id = args['game_id']
    score = args['score']

    myquery = {'_id': game_id}
    what_i_want = {'users': 1}

    response = {'error': False}

    #ADD_USER = 0
    if req_type == ADD_USER:
      #get the game from DB
      try:
        res = db.find(myquery)
      except:
        response['error'] = True
        return Response(response, status=400)

      users = list(res)[0]['users']

      #if user already in games do not reset score
      if who not in users:
        users[who] = 0

      newvalues = {'$set': {'users': users}}
      #add a user in the game
      try:
        res = db.update_one(myquery, newvalues)
      except:
        response['error'] = True
        return Response(response, status=400)

    #SCORE = 1
    if req_type == SCORE:
      if score == None:
        response['error'] = True
        return Response(response, status=400)

      #get the game from DB
      try:
        res = db.find(myquery, what_i_want)
      except:
        response['error'] = True
        return Response(response, status=400)
      
      users = list(res)[0]['users']

      try:
        users[who] = score
      except:
        response['error'] = True
        return Response(response, status=400)

      newvalues = {'$set': {'users': users}}
      #add a user in the game
      try:
        res = db.update_one(myquery, newvalues)
      except:
        response['error'] = True
        return Response(response, status=400)

    return response

  #CANCEL
  #/?game_id=G_ID&who=UID
  def delete(self):
    args = parsecancel.parse_args()
    who = args['who']
    game_id = args['game_id']

    myquery = {'_id': game_id}
    what_i_want = {'users': 1}

    response = {'error': False}

    #get the game from DB
    try:
      res = db.find(myquery, what_i_want)
    except:
      response['error'] = True
      return Response(response, status=400)

    users = list(res)[0]['users']

    try:
      users.pop(who)
    except:
      response['error'] = True
      return Response(response, status=400)

    newvalues = {'$set': {'users': users}}
    #add a user in the game
    try:
      res = db.update_one(myquery, newvalues)
    except:
      response['error'] = True
      return Response(response, status=400)

    return response

#create a random string
def generate_string():
  rnd = ''.join(random.choice(string.ascii_lowercase + string.digits) for _ in range(6))
  return rnd


#From the list of users in a game <user, score>, 
#generate a dictionary <score, [user1, user2,...]>.
def generate_leaderboard(users):
  leaderboard = {}

  # key = user, value = score
  for user,score in users.items():
    #usr = auth.get_user(user)
    #name_surname = usr.display_name
    name_surname = user
    if score in leaderboard.keys():
      users = leaderboard[score]
      users.append(name_surname)
      leaderboard[score] = users
    else:
      leaderboard[score] = [name_surname]

  return leaderboard

api.add_resource(Games, "/")

if __name__ == "__main__":
  print("API started")
  app.run(host = "0.0.0.0")
