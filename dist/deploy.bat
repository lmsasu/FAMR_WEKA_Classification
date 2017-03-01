rd /s /q FAMR_Classification
del FAMR_Classification.zip
rd /S /Q %USERPROFILE%\wekafiles\packages\FAMR_Classification
mkdir %USERPROFILE%\wekafiles\packages\FAMR_Classification
del /F /Q %USERPROFILE%\wekafiles\packages\FAMR_Classification\*.*
del d:\temp\FAMR_wekalog.txt
call ant -buildfile ..\build_package.xml
xcopy /Q /S /I FAMR_Classification %USERPROFILE%\wekafiles\packages\FAMR_Classification