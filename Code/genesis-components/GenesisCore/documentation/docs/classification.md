# Classification

Genesis uses WordNet to get classification threads of entities.

## Ordinary classification

If you want to associate a name with a single thread, write as follows:

`John is a person.`

The result is that John has just one thread:

`thing entity physical-entity object whole living-thing organism person name john`

Note that the final element in the thread is the name of the entity; the penultimate element is a marker that signals a name. This convention is exploited by the matcher.

On the other hand, if you want a name to have an additional thread, you write:

`John is also a criminal.`

The result is that John has an added thread:

```
thing entity physical-entity object whole living-thing organism person name john

thing entity physical-entity object whole living-thing organism person bad-person wrongdoer principal criminal name john
```

Again, note that the final elements in the threads are the name of the entity; the penultimate element is a marker that signals a name.

Finally, maybe you want to define a class not in wordnet:

`A bouvier is a kind of dog.`

The result is:

```
thing entity physical-entity object whole living-thing organism animal chordate vertebrate

mammal placental carnivore canine dog bouvier
```

Note that there is no name marker.


## Assert classification

Sometimes you will want to specify a complete thread for a word or phrase. This first come up in persuasive story telling when Sila Sayan wanted to declare many properties that make someone likable or unlikable.

```
Assert thread thing, likable, good parent.
Assert thread thing, likable, caring.
```

It also became useful to be able to specify opposites:

`Likable is the opposite of unlikable.`


## Specify classification.

Suppose you say:

`A rat scared Mary.`

Alas, wordnet supplies five meanings for rat, three of which are subclasses of people. To order Genesis to limit itself to the three, you can use the following convention:

`A (rat person) scared Mary.`

And if you really want to be specific, you can write:

`A (rat unpleasant-person) scared Mary.`


## Specify new names and gender

Suppose you want to introduce a name, such as `SuspectA`, that START does not know about. Here is what you could do:

```
Note that SuspectA is a name.
SuspectA is a suspect.
```

Generally, however, it is better to supply a gender. Otherwise the use of pronouns will not work, as in “SuspectA killed himself;” the problem is that if you do not supply gender, SuspectA will be neuter and START will not substitute SuspectA for himself. So, here is how you supply gender:

`Note that SuspectA is a masculine name.`

or

`Note that SuspectA is a feminine name.`
