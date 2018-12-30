Skeleton application to respond to survey type questions over missed calls.

To install and run the application.
First of all clone the repository into your local machine.
Then install Android Studio.
In Android Studio import this directory as a project. (It should automatically identify this as an android studio project.)
Connect your android device and just install it.

The file MainActivity.java contains the code for the application. This version of application tailored to facilitate the measurement process, this includes generation of files that log timestamps. So its highly recommended that you go over the base implementation first before you start extending the code base.

note: textMessages intended for the application should be appended by "mcqApp:" in the begining and the content should be of the following format - "question":"option1":"option2":"option3":"option4".