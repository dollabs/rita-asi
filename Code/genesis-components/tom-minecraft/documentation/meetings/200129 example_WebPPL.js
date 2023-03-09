var actionPrior = Categorical({vs: ['a', 'b'], ps: [.5, .5]})
var haveCookie = function(obj) {return obj == 'cookie'};

var chooseAction = function(goalSatisfied, transition, state) {
  return Infer({method: 'enumerate'}, function() {
    var action = sample(actionPrior)
    condition(goalSatisfied(transition(state, action)))
    return action;
  })
}
///
var vendingMachine = function(state, action) {
  return (action == 'a' ? categorical({vs: ['bagel', 'cookie'], ps: [.9, .1]}) :
          action == 'b' ? categorical({vs: ['bagel', 'cookie'], ps: [.1, .9]}) :
          'nothing');
}

var goalPosterior = Infer({method: 'MCMC', samples: 20000}, function() {
  var preference = uniform(0, 1);
  var goalPrior = function() {return flip(preference) ? 'bagel' : 'cookie'};
  var makeGoal = function(food) {return function(outcome) {return outcome == food}};
  condition((sample(chooseAction(makeGoal(goalPrior()), vendingMachine, 'state')) == 'b') &&
            (sample(chooseAction(makeGoal(goalPrior()), vendingMachine, 'state')) == 'b') &&
            (sample(chooseAction(makeGoal(goalPrior()), vendingMachine, 'state')) == 'b'));
  return goalPrior();
})

viz.auto(goalPosterior);