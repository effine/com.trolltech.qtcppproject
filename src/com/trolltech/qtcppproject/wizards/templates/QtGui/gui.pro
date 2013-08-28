TEMPLATE = app
TARGET = %PROJECT% 

QT        += %MODULES%

HEADERS   += %HEADER_FILE%
SOURCES   += main.cpp \
    %SOURCE_FILE%
FORMS     += %UI_FILE% 
RESOURCES +=

# save custom varible (Target Compile Platform)
# The User already Buied

TARGET_PLATFORM +=%TargetPlatform%