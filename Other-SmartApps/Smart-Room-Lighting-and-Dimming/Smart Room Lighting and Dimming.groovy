/**
 *  Smart Room Lighting and Dimming-Parent
 *
 *  Version - 1.0.0 5/4/15
 *  Version - 1.0.2 5/19/15 Code clean up for timeframe conditional check
 *  Version - 1.0.4 5/31/15 Added About screen from main menu
 *  Version - 1.0.5 6/17/15 Code optimization
 *  Version - 1.1.0 7/4/15 Added more dynamic interface options and the ability to utilize color controlled lights
 *  Version - 1.2.0 8/28/15 Added option to turn off dimmers if set to anything above 0 when lux threshold is exceeded
 *  Version - 2.0.0 11/24/15 Modified to allow more scenarios via parent/child app structure
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
    name: "Smart Room Lighting and Dimming",
    singleInstance: true,
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Control multiple scenarios of light/dimmers based on motion and lux levels.",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Smart-Room-Lighting-and-Dimming/SmartLight.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Smart-Room-Lighting-and-Dimming/SmartLight@2x.png",
	iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Smart-Room-Lighting-and-Dimming/SmartLight@2x.png"
    )
    
preferences {
    page(name: "mainPage", title: "Room Scenarios", install: true, uninstall: true,submitOnChange: true) {
            section {
                    app(name: "childScenarios", appName: "Smart Room Lighting and Dimming-Scenario", namespace: "MichaelStruck", title: "Create New Scenario...", multiple: true)
            }
            section([title:"Options", mobileOnly:true]) {
            	label title:"Assign a name", required:false
            	href "pageAbout", title: "About ${textAppName()}", description: "Tap to get application version, license and instructions"
        	}
    }
}

page(name: "pageAbout", title: "About ${textAppName()}") {
        section {
            paragraph "${textVersion()}\n${textCopyright()}\n\n${textLicense()}\n"
        }
        section("Instructions") {
            paragraph textHelp()
        }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    childApps.each {child ->
            log.info "Installed Scenario: ${child.label}"
    }
}

//Version/Copyright/Information/Help

private def textAppName() {
	def text = "Smart Room Lighting and Dimming"
}	

private def textVersion() {
    def text = "Version 2.0.0 (11/24/2015)"
}

private def textCopyright() {
    def text = "Copyright © 2015 Michael Struck"
}

private def textLicense() {
    def text =
		"Licensed under the Apache License, Version 2.0 (the 'License'); "+
		"you may not use this file except in compliance with the License. "+
		"You may obtain a copy of the License at"+
		"\n\n"+
		"    http://www.apache.org/licenses/LICENSE-2.0"+
		"\n\n"+
		"Unless required by applicable law or agreed to in writing, software "+
		"distributed under the License is distributed on an 'AS IS' BASIS, "+
		"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. "+
		"See the License for the specific language governing permissions and "+
		"limitations under the License."
}

private def textHelp() {
	def text =
        "Within each scenario you create you can select motion sensors to control a set of lights. " +
        "Each scenario can control dimmers or switches and can also be restricted " +
        "to modes or between certain times and turned off after motion " +
        "motion stops. Scenarios can also be limited to running once " +
        "or to stop running if the physical switches are turned off."+
        "\n\nOn the dimmer options page, enter the 'on' or 'off' levels for the dimmers. You can choose to have the " +
        "dimmers' level calculated between the 'on' and 'off' settings " +
        "based on the current lux value. In other words, as it gets " +
        "darker, the brighter the light level will be when motion is sensed."
}
