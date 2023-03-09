# Git - Common Nightmares

It can be stressful to tackle problems with git because you risk destroying the team's codes. It's always good to search on Google/ Slack Overflow to find solutions and understand what the commands in the solutions mean.

Here let's include the issues we've encountered and our solutions to fix them.

## Tried to push a large file and cannot push again

[Can't remove file from git commit](https://stackoverflow.com/questions/21168846/cant-remove-file-from-git-commit)

I solved by

```
git filter-branch -f --index-filter "git rm -rf --cached --ignore-unmatch FOLDERNAME" -- --all
git add *
git commit -m 'fix large files'
git pull origin master --allow-unrelated-histories
git push
```
