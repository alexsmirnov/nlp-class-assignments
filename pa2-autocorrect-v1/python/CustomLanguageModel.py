import math, collections


class CustomLanguageModel:

  def __init__(self, corpus):
    """Initialize your data structures in the constructor."""
    self.trigrams = collections.Counter()
    self.bigrams = collections.Counter()
    self.words = collections.Counter()
    self.total = 0
    self.train(corpus)

  def train(self, corpus):
    """ Takes a corpus and trains your language model. 
        Compute any counts or other corpus statistics in this function.
    """  
    for sentence in corpus.corpus:
      data = sentence.data
      token1 = data[0].word
      token2 = data[1].word
      for datum in data[2:]:  
        token3 = datum.word
        bg = (token1,token2)
        self.words[token1] += 1
        self.bigrams[bg] += 1
        self.trigrams[(token1,token2,token3)] += 1
        self.total += 1
        token1 = token2 
        token2 = token3 

  def score(self, sentence):
    """ Takes a list of strings as argument and returns the log-probability of the 
        sentence using your language model. Use whatever data you computed in train() here.
    """
    score = 0.0
    word1 = sentence[0]
    word2 = sentence[1]
    for word3 in sentence[2:] :
      bg = (word1,word2,word3)
      count1 = self.words[word1]
      count2 = self.bigrams[(word1,word2)]
      count3 = self.trigrams[bg]
      if count3 > 0 :
        score += math.log(count3)
        score -= math.log(count2)
      elif count2 > 0 :
        score += math.log(count2)  + math.log(0.4)
        score -= math.log(count1)
      else :
        score += math.log(count1 + 1) + math.log(0.4)*2.0
        score -= math.log(self.total + len(self.words))
      word1 = word2
      word2 = word3
    return score


class BiGram:
  def __init__(self,first,second):
    self.first = first
    self.second = second

  def __hash__(self):
    return hash(self.first) + 31 * hash(self.second)

  def __eq__(self,other):
    return self.first == other.first and self.second == other.second
