# TermCalc
A fully customizable Android calculator app, with various advanced features and functions. 

## Theme Customization
There are two ways to modify TermCalc's appearance. The first is through the basic theme menu, which simply allows the user to select a light or dark theme, then an accent color. The second is using the custom theme editor. When using the latter, any item on the home screen of the application can be customized. Both the background and text colors of each element can be modified, either using a color picker, or entering a specific hex code. Entire zones (like the number keypad, for example) can also be modified at once to speed-up theme creation. Custom themes can also intelligently modify the application's nav drawer (adaptively determining which colors in the theme would clash the least with the background color of the nav drawer), and other sections of the application (i.e. Unit Converter). Users can also switch between round, or square/borderless buttons when using either theme editor (round buttons are currently a WIP, but should be an option in the next update). Finally, custom themes can also be saved, restored, and shared with others at any time.

## Mathematical and Technical Features
The app uses BigDecimal precision (or rather, will once I finish the next update, sigh) for most calculations (mainly those that would specifically benefit from such precision), and has advanced functionality including:
 - Inverse hyperbolic trigonometry
 - Custom user-created functions (i.e. Compound Interest or Permutation)
 - A list of common constants (i.e. Avogadro's Number), and the ability to add more
 - Date Calculator
 - Unit and Currency Converter
 - N-th root
 - Log base n
 - Modulus
 - Various geometric formulas (i.e. Volume, Surface Area) that can be evaluated after variable values are entered.
 - User-defined maximum precision
 - An option to ignore PEMDAS and prioritize coefficients instead

The application also has a somewhat hidden terminal interface (hence the terrible name "_Term_ Calc") that allows the user to perform a few specific niche actions, like changing the text of the modulus button to "%" as opposed to "mod," or printing a stack trace after a crash. This used to be a more integral part of the application, which is why the application is named the way it is, but as I've grown as a developer, I've created GUIs for most of the more useful functionality that the terminal provides.

###### Terminal Commands
The list below outlines all currently-supported terminal commands that are intended for use by end-users:
 - help: displays basic commands, essentially this list until the "themes" command
 - help \[command]: prints information about a specific command
 - set \[button code] \[color hex]: sets a color in the current theme (for a quick guide on button codes, run "help set")
 - get \[button code]: prints a color in the current theme
 - sym -mod \[any text]: sets the text of the mod button
 - reset \[button code]: resets a color in the current theme to its default value
 - copy \[button code]: copies a color in the current theme to the devices keyboard
 - share \[theme name]: export a theme
 - mode \[theme number]: changes the current base theme (light, dark, etc. based on the order of these themes in the Basic theme editor)
 - delete \[theme name]: deletes the selected custom theme
 - recreate: restarts the home layout
 - themes: lists all backed-up themes
 - debug stack: prints the stack trace of the last recorded crash
 - debug reason: only prints the reason for the last recorded crash

## Download
https://play.app.goo.gl/?link=https://play.google.com/store/apps/details?id=com.mirambeau.termcalc&ddl=1&pcampaignid=web_ddl_1
