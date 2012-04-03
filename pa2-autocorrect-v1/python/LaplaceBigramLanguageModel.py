import math, collections


class LaplaceBigramLanguageModel:

  def __init__(self, corpus):
    """Initialize your data structures in the constructor."""
    self.bigramms = collections.Counter()
    self.words = collections.Counter()
    self.total = 0
    self.train(corpus)

  def train(self, corpus):
    """ Takes a corpus and trains your language model. 
        Compute any counts or other corpus statistics in this function.
    """  
    for sentence in corpus.corpus:
      data = sentence.data
      for datum1,datum2 in zip(data,data[1:]):  
        token1 = datum1.word
        token2 = datum2.word
        bg = BiGramm(token1,token2)
        self.words[token1] += 1
        self.bigramms[bg] += 1
        self.total += 1
 

  def score(self, sentence):
    """ Takes a list of strings as argument and returns the log-probability of the 
        sentence using your language model. Use whatever data you computed in train() here.
    """
    score = 0.0
    for word1,word2 in zip(sentence,sentence[1:]):
      bg = BiGramm(word1,word2)
      count = self.bigramms[bg]
      score += math.log(count+1)
      score -= math.log(self.total) 
    return score


class BiGramm:
  def __init__(self,first,second):
    self.first = first
    self.second = second

  def __hash__(self):
    return hash(self.first) + 31 * hash(self.second)

  def __eq__(self,other):
    return self.first == other.first and self.second == other.second
