/**
 *  Smart Room Lighting and Dimming-Scenario
 *
 *  Version 1.0.0 (11/24/15) - Initial release of child app
 *  Version 1.0.1 (11/29/15) - Code opimization
 *
 *  Copyright 2015 Michael Struck - Uses code from Lighting Director by Tim Slagle & Michael Struck
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 
definition(
    name: "Smart Room Lighting and Dimming-Scenario",
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Child app (do not publish) that allows for control of light/dimmers based on motion and lux levels.",
    category: "Convenience",
    parent: "MichaelStruck:Smart Room Lighting and Dimming",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Smart-Room-Lighting-and-Dimming/SmartLight.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Smart-Room-Lighting-and-Dimming/SmartLight@2x.png",
	iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Smart-Room-Lighting-and-Dimming/SmartLight@2x.png"
    )

preferences {
    page name:"pageSetup"
}

// Show setup page
def pageSetup() {
    dynamicPage(name: "pageSetup", install: true, uninstall: true) {
		section("Name your scenario") {
			label title:"Scenario Name", required:true
    	}
        section("Devices included in the scenario") {
    		input "A_motion", "capability.motionSensor", title: "Using these motion sensors...", multiple: true, required: false
        	input "A_switches", "capability.switch",title: "Control the following switches...", multiple: true, required: false
        	input "A_dimmers", "capability.switchLevel", title: "Dim the following...", multiple: true, required:false, submitOnChange:true
        	input "A_colorControls", "capability.colorControl", title: "Control the following colored lights...",  multiple: true, required: false, submitOnChange:true
		}

		section("Lighting settings") {
        	if (A_dimmers){
        		href "levelInputA", title: "Dimmer Options", description: getLevelLabel(A_levelDimOn, A_levelDimOff, A_calcOn), state: "complete"
        	}
        	if (A_colorControls){
        		href "colorInputA", title: "Color Options", description: getColorLabel(A_levelDimOnColor, A_levelDimOffColor, A_calcOnColor, A_color), state: "complete"
        	}
        	input name: "A_turnOnLux",type: "number",title: "Only run this scenario if lux is below...", multiple: false, required: false
        	input name: "A_luxSensors",type: "capability.illuminanceMeasurement",title: "On these lux sensors",multiple: false,required: false, submitOnChange:true
        	input name: "A_turnOff",type: "number",title: "Turn off this scenario after motion stops (minutes)...", multiple: false, required: false
        	if (A_luxSensors && A_levelDimOff > 0 && A_turnOnLux){
        		input name: "A_turnOffLux", type: "bool", title: "Turn off dimmers when lux above threshold", defaultValue: false
        	}
		}
            
		section("Restrictions") {            
        	input name: "A_triggerOnce",type: "bool", title: "Trigger on only once per day...", defaultValue: false, submitOnChange:true
            if (A_triggerOnce) {
                input name: "A_triggerOnceOff", type: "bool", title: "Trigger off only once per day...", defaultValue: false
        	}
            input name: "A_switchDisable", type:"bool", title: "Stop triggering if physical switches/dimmers are turned off...", defaultValue:false
        	href "timeIntervalInputA", title: "Only during a certain time...", description: getTimeLabel(A_timeStart, A_timeEnd), state: greyedOutTime(A_timeStart, A_timeEnd)
        	input name:  "A_day", type: "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only on certain days of the week...",  multiple: true, required: false
        	input name: "A_mode", type: "mode", title: "Only during the following modes...", multiple: true, required: false
		}
        section("About ${textAppName()}") { 
			paragraph "${textVersion()}\n${textCopyright()}"
    	}
    }
}

page(name: "timeIntervalInputA", title: "Only during a certain time") {
		section {
			input "A_timeStart", "time", title: "Starting", required: false
			input "A_timeEnd", "time", title: "Ending", required: false
		}
}  

page(name: "levelInputA", title: "Set dimmers options...") {
		section {
			input name: "A_levelDimOn", type: "number", title: "On Level", multiple: false, required: false
        	input name: "A_levelDimOff", type: "number", title: "Off Level", multiple: false, required: false
			input name: "A_calcOn",type: "bool",title: "Calculate 'on' level via lux", defaultValue: false
        }
}

page(name: "colorInputA", title: "Set colored light options...") {
		section {
			input "A_color", "enum", title: "Choose a color", required: false, multiple:false, options: [
					["Soft White":"Soft White"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
            input "A_levelDimOnColor", "number", title: "On Level", multiple: false, required: false
        	input "A_levelDimOffColor", "number", title: "Off Level", multiple: false, required: false
			input "A_calcOnColor", "bool", title: "Calculate 'on' level via lux", defaultValue: false
        }
}

//----------------------
def installed() {
    initialize()
}

def updated() {
    unschedule()
    unsubscribe()
    initialize()
}

def initialize() {

	midNightReset()

	if(A_motion) {
		subscribe(A_motion, "motion", onEventA)
	}

	if (A_luxSensors && A_levelDimOff > 0 && A_turnOnLux && A_turnOffLux){
    	subscribe(A_luxSensors, "illuminance", turnOffLuxA)
	}

	if(A_switchDisable) {
		subscribe(A_switches, "switch.off", onPressA)
    	subscribe(A_dimmers, "switch.off", onPressA)
    	subscribe(A_colorControls, "switch.off", onPressA)
	}
}

def onEventA(evt) {
if ((!A_triggerOnce || (A_triggerOnce && !state.A_triggered)) && (!A_switchDisable || (A_switchDisable && !state.A_triggered))) {  	
if ((!A_mode || A_mode.contains(location.mode)) && getTimeOk (A_timeStart, A_timeEnd) && getDayOk(A_day)) {
	if (!A_luxSensors || (A_luxSensors.latestValue("illuminance") <= A_turnOnLux)){
    	if (A_motion.latestValue("motion").contains("active")) {
        	
        		log.debug("Motion Detected Running '${ScenarioNameA}'")
            
            	def levelSetOn = A_levelDimOn ? A_levelDimOn : 100
                def levelSetOnColor = A_levelDimOnColor ? A_levelDimOnColor : 100
            	def levelSetOff = A_levelDimOff ? A_levelDimOff : 0
                def levelSetOffColor = A_levelDimOffColor ? A_levelDimOffColor : 0

            	if (A_calcOn && A_luxSensors) {
    				levelSetOn = (levelSetOn * (1-(A_luxSensors.latestValue("illuminance")/A_turnOnLux))) + levelSetOff
                	if (levelSetOn > 100) {
               			levelSetOn = 100
               		}
    			}
                if (A_calcOnColor && A_luxSensors) {
    				levelSetOnColor = (levelSetOnColor * (1-(A_luxSensors.latestValue("illuminance")/A_turnOnLux))) + levelSetOffColor
                	if (levelSetOnColor > 100) {
               			levelSetOnColor = 100
               		}
    			}
        		A_dimmers?.setLevel(levelSetOn)
                setColoredLights(A_colorControls, A_color, levelSetOnColor)
        		A_switches?.on()
        		if (A_triggerOnce && !A_triggerOnceOff){
           			state.A_triggered = true
            		if (!A_turnOff) {
						runOnce (getMidnight(), midNightReset)
            		}
				}
				if (state.A_timerStart){
           			unschedule(delayTurnOffA)
       	   			state.A_timerStart = false
        		}	
		}
		else {
    		if (A_turnOff) {
				runIn(A_turnOff * 60, "delayTurnOffA")
        		state.A_timerStart = true
        	}
        	else {
        		if (A_triggerOnce && A_triggerOnceOff){
           			state.A_triggered = true
                }
                A_switches?.off()
        		def levelSetOff = A_levelDimOff ? A_levelDimOff : 0
        		A_dimmers?.setLevel(levelSetOff)
                def levelSetOffColor = A_levelDimOffColor ? A_levelDimOffColor : 0
                A_colorControls?.setLevel(levelSetOffColor)
        		if (state.A_triggered) {
    				runOnce (getMidnight(), midNightReset)
    			}
        	}
		}
	}
}
else{
	log.debug("Motion outside of mode or time/day restriction.  Not running scenario.")
}
}
}

def delayTurnOffA(){
	if (A_triggerOnce && A_triggerOnceOff){
		state.A_triggered = true
    }
    A_switches?.off()
	def levelSetOff = A_levelDimOff ? A_levelDimOff : 0
    A_dimmers?.setLevel(levelSetOff)
    def levelSetOffColor = A_levelDimOffColor ? A_levelDimOffColor : 0
    A_colorControls?.setLevel(levelSetOffColor)
	state.A_timerStart = false
	if (state.A_triggered) {
    	runOnce (getMidnight(), midNightReset)
    }
}

def turnOffLuxA(evt){
	if (A_luxSensors.latestValue("illuminance") > A_turnOnLux){
    	A_dimmers?.setLevel(0)
        unschedule(delayTurnOffA)
        state.A_timerStart = false
    }
}

def onPressA(evt) {
	if ((!A_mode || A_mode.contains(location.mode)) && getTimeOk (A_timeStart, A_timeEnd) && getDayOk(A_day)) {
		if (!A_luxSensors || (A_luxSensors.latestValue("illuminance") <= A_turnOnLux)){
			if ((!A_triggerOnce || (A_triggerOnce && !state.A_triggered)) && (!A_switchDisable || (A_switchDisable && !state.A_triggered))) {	
    			if (evt.physical){
    				state.A_triggered = true
        			unschedule(delayTurnOffA)
        			runOnce (getMidnight(), midNightReset)
        			log.debug "Physical switch in '${ScenarioNameA}' pressed. Trigger for this scenario disabled."
				}
			}
		}
	}
}

//Common Methods

def midNightReset() {
	state.A_triggered = false
}

def greyedOutTime(start, end){
	def result = start || end ? "complete" : ""
}

def getMidnight() {
	def midnightToday = timeToday("2000-01-01T23:59:59.999-0000", location.timeZone)
	midnightToday
}

private getTimeOk(startTime, endTime) {
	def result = true
	if (startTime && endTime) {
		def currTime = now()
		def start = timeToday(startTime).time
		def stop = timeToday(endTime).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	else if (startTime){
    	result = currTime >= start
    }
    else if (endTime){
    	result = currTime <= stop
    }
    result
}

def getTimeLabel(start, end){
	def timeLabel = "Tap to set"
	
    if(start && end){
    	timeLabel = "Between" + " " + hhmm(start) + " "  + "and" + " " +  hhmm(end)
    }
    else if (start) {
		timeLabel = "Start at" + " " + hhmm(start)
    }
    else if(end){
    timeLabel = "End at" + hhmm(end)
    }
	timeLabel	
}

def getLevelLabel(on, off, calcOn) {
	def levelLabel="'On' level: "
	if (!on) {
		on= 100
	}
	if (!off) {
		off = 0
    }
	if (calcOn) {
		levelLabel = levelLabel + "Between ${off}% and ${on}% based on lux"
	}
	else {
        if (on) {
    		levelLabel = levelLabel + "${on}%"
    	}
	}
	levelLabel = levelLabel + "\n'Off' level: ${off}%" 
    levelLabel
}

def getColorLabel(on, off, calcOn, color) {
    def levelLabel = color ? "Color: ${color}" : "Color: Soft White" 
    levelLabel += "\n'On' level: "
	if (!on) {
		on= 100
	}
	if (!off) {
		off = 0
    }
	if (calcOn) {
		levelLabel = levelLabel + "Between ${off}% and ${on}% based on lux"
	}
	else {
        if (on) {
    		levelLabel = levelLabel + "${on}%"
    	}
	}
	levelLabel = levelLabel + "\n'Off' level: ${off}%" 
    
    levelLabel
}

private setColoredLights(colorControls, color, lightLevel){
   	def chooseColor = color ? "${color}" : "Soft White"
    def hueColor = 0
	def saturationLevel = 100
	switch(chooseColor) {
		case "White":
			hueColor = 52
			saturationLevel = 19
			break;
		case "Daylight":
			hueColor = 52
			saturationLevel = 16
			break;
		case "Soft White":
			hueColor = 23
			saturationLevel = 56
			break;
		case "Warm White":
			hueColor = 13
			saturationLevel = 30 
			break;
		case "Blue":
			hueColor = 64
			break;
		case "Green":
			hueColor = 37
			break;
		case "Yellow":
			hueColor = 16
			break;
		case "Orange":
			hueColor = 8
			break;
		case "Purple":
			hueColor = 78
			break;
		case "Pink":
			hueColor = 87
			break;
		case "Red":
			hueColor = 100
			break;
	}
    def newValue = [hue: hueColor as Integer, saturation: saturationLevel as Integer, level: lightLevel as Integer]
    colorControls?.setColor(newValue)
}


private hhmm(time) {
	new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", time).format("h:mm a", timeZone(time))
}

private getDayOk(dayList) {
	def result = true
    if (dayList) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = dayList.contains(day)
	}
    result
}

private def textAppName() {
	def text = "Smart Room Lighting and Dimming-Scenario"
}	

private def textVersion() {
    def text = "Version 1.0.1 (11/28/2015)"
}

private def textCopyright() {
    def text = "Copyright © 2015 Michael Struck"
}
