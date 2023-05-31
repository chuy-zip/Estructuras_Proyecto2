import os
from dotenv import load_dotenv
from neo4j import GraphDatabase
from apps.DataBaseDriver import *

class DataBaseController:
    def __init__(self):
        load_dotenv('./.env')
        self.URI = os.getenv("NEO4J_URI")
        self.USER = os.getenv("NEO4J_USERNAME")
        self.PASSWORD = os.getenv("NEO4J_PASSWORD")
        self.currentUser = None
        self.filteredGames = []
        self.recommendedGames = []

    def get_user_played_games(self):
        return self.currentUser.played_games

    def get_user_favorite_games(self):
        return self.currentUser.favorite_games

    def account_exists(self, name, password):
        with DataBaseDriver(self.URI, self.USER, self.PASSWORD, Config.default_config()) as app:
            if not app.user_exists(name, password):
                return True
        return False

    def valid_login(self, name, password):
        with DataBaseDriver(self.URI, self.USER, self.PASSWORD, Config.default_config()) as app:
            if self.account_exists(name, password):
                self.set_current_user_from_database(name, password)
                self.set_filtered_games_from_database()
                self.filteredGames = self.get_filtered_games_by_esrb(self.filteredGames, self.currentUser)

    def set_current_user_from_database(self, name, password):
        with DataBaseDriver(self.URI, self.USER, self.PASSWORD, Config.default_config()) as app:
            if app.user_exists(name, password):
                user_node = app.find_user_node(name, password)
                self.currentUser = User(
                    user_node.name,
                    user_node.password,
                    user_node.age,
                    user_node.prefers_nintendo,
                    user_node.prefers_pc,
                    user_node.prefers_mobile,
                    user_node.prefers_xbox,
                    user_node.prefers_playstation,
                    user_node.prefers_multiplayer,
                    user_node.played_games,
                    user_node.favorite_games
                )

    def set_filtered_games_from_database(self):
        with DataBaseDriver(self.URI, self.USER, self.PASSWORD, Config.default_config()) as app:
            if self.currentUser is not None:
                self.filteredGames = app.get_compatible_games(self.currentUser)

    def valid_sign_in(self, name, password, age, prefer_nintendo, prefer_pc, prefer_mobile, prefer_xbox,
                      prefer_playstation, prefer_multi):
        with DataBaseDriver(self.URI, self.USER, self.PASSWORD, Config.default_config()) as app:
            if not self.account_exists(name, password):
                app.create_persona_node(name, age, password, prefer_nintendo, prefer_pc, prefer_mobile, prefer_xbox,
                                        prefer_playstation, prefer_multi)
                self.valid_login(name, password)

    def get_filtered_games_by_esrb(self, games, user):
        user_age = user.user_age
        permitted_games = []

        for current_game in games:
            game_rating = current_game.esrb_rating

            if user_age < 17 and user_age >= 13:
                if game_rating != "M":
                    permitted_games.append(current_game)
            elif user_age < 13 and user_age >= 10:
                if game_rating != "M" and game_rating != "T":
                    permitted_games.append(current_game)
            elif user_age < 10:
                if game_rating != "M" and game_rating != "T" and game_rating != "E10+":
                    permitted_games.append(current_game)
            else:
                # For users 17 and above, all games are permitted
                permitted_games.append(current_game)

        return permitted_games

    def get_games_filtered_by_category(self, categories):
        new_filtered_games = []

        for game in self.filteredGames:
            if game.categories[0] in categories or game.categories[1] in categories or game.categories[2] in categories:
                new_filtered_games.append(game)

        return new_filtered_games