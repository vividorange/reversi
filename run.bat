cd src
for /l %%a in (1, 1, 50) do (
 java RandomReversi
 java RandomReversi a
)
exit