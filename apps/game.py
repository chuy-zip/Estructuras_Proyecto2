class Game:
    def __init__(self, game_name, description, duration, category1, category2, category3, is_on_nintendo,
                 is_on_pc, is_on_mobile, is_on_xbox, is_on_playstation, is_multiplayer, esrb_rating):
        self.game_name = game_name
        self.description = description
        self.duration = duration
        self.categories = [category1, category2, category3]
        self.is_on_nintendo = is_on_nintendo
        self.is_on_pc = is_on_pc
        self.is_on_mobile = is_on_mobile
        self.is_on_xbox = is_on_xbox
        self.is_on_playstation = is_on_playstation
        self.is_multiplayer = is_multiplayer
        self.esrb_rating = esrb_rating