		Date.prototype.addHours = function(h) {
			var ret = new Date()
			ret.setTime(this.getTime() + (h*60*60*1000)); 
			return ret;   
		}
		
		var Wochentag = new Array("Sonntag", "Montag", "Dienstag", "Mittwoch",
                          "Donnerstag", "Freitag", "Samstag");
						  
		var actualImageNo = 0;
		
		var lastRefresh;
		
		var FirstImgDelay = 6;
		var ImageCount = 49;
		var SliderPartWidth = 13;
		var lastSliderPos = -1;
		
		var SliderPartName = "sliderpart";
		
		var displayWaterLevel = false;
		
		var displayCallback;
		
		function init(startCallback, displCallback) {
			displayCallback = displCallback
			initSlider()
			var now = new Date().addHours(0.5);
			var hours = now.getUTCHours()
			if (hours < 6) {
				hours = hours + 6
			} else if (hours >= 12 && hours < 18) {
				hours = hours - 6
			} else if (hours >= 18) {
				hours = hours -12
			}
			initLastRefreshAndDisplay(now.addHours(-hours), startCallback);
		}

		function initLastRefreshAndDisplay(date, startCallback) {
			img = new Image()
			img.onload = function() {
				setLastRefresh(date)
				startCallback()
			}
			img.onerror = function() {
				initLastRefreshAndDisplay(date.addHours(-6), startCallback)
			}
			img.src = imageLink("Wind", date, getDate(date, 0))
		}
		
		function initSlider() {
			for (var i = 0; i < ImageCount; i++) {
				var div = document.createElement("div");
				div.className = SliderPartName;
				div.style.width = SliderPartWidth + "px";
				div.id = "" + i
				div.onmouseover = function() {
					var nr = parseInt(this.id);
					setActImage(nr);
				}
				div.ontouchmove = function(e) {
					var imgNo = e.touches[0].screenX / SliderPartWidth
					setActImage(imgNo.toFixed() - 1)
					e.stopPropagation()
					e.preventDefault()
				}
				document.getElementById('navslider').appendChild(div);
			}
		}
		function displaySliderPos(imgNo) {
			if (lastSliderPos >= 0) {
				getSliderPart(lastSliderPos).style.backgroundColor="#aaa"
			}
			getSliderPart(imgNo).style.backgroundColor="#555"
			lastSliderPos = imgNo;
		}
		
		function getSliderPart(no) {
			return document.getElementById("" + no)
		}
		
		function setActImage(nr) {
			if (nr != actualImageNo) {
				actualImageNo = nr
				displayCallback();
			}
		}
		
		function zweistellig(zahl) {
			if (zahl > 9) return "" + zahl 
			else return "0" + zahl
		}
		
		function dateUrlStr(dateParam) {
			return dateParam.getUTCFullYear() + zweistellig(dateParam.getUTCMonth()+1) 
					+ zweistellig(dateParam.getUTCDate()) + "_" + zweistellig(dateParam.getUTCHours()) + "00"
		}
		
		function imageLink(type, refreshDate, imgDate) {
			return "http://www.dmi.dk/fileadmin/SeaForecast/vestlige_osterso/" + type 
					+ "." + dateUrlStr(refreshDate) + "." + dateUrlStr(imgDate) + ".png"
		}
		
		function getDate(refreshDate, imgNo) {
			return refreshDate.addHours(FirstImgDelay + imgNo)
		}
		
		function nextImage(count) {
			setActImage(actualImageNo + count)
		}
		
		function dateDisplStr(date) {
			return date.getHours() + ":00"
		}
		
		function display() {
			displaySliderPos(actualImageNo)
			var imgDate = getDate(lastRefresh, actualImageNo)
			document.getElementById("Wind").src = displayWaterLevel
					? imageLink("Waterlevel", lastRefresh, imgDate)
					: imageLink("Wind", lastRefresh, imgDate)
			if (!displayWaterLevel) {
				document.getElementById("WindDirection").src = imageLink("WindDirection", lastRefresh, imgDate)
			}
			displLabel(imgDate)
		}
		
		function displayLabel() {
			displLabel(getDate(lastRefresh, actualImageNo))
		}
		function displLabel(imgDate) {
			document.getElementById("datelabel").innerHTML = Wochentag[imgDate.getDay()] + ", "
					+ dateDisplStr(imgDate) + "   "
		}
		
		function preloadImage(imgNo) {
			var tmpDate = getDate(lastRefresh, i)
			new Image().src = imageLink("Wind", lastRefresh, tmpDate)
			new Image().src = imageLink("WindDirection", lastRefresh, tmpDate)
		}
		
		function loadAllImages() {
			for (var i = 1; i <= ImageCount; i++) {
				preload(i)
			}
		}
		
		function getFirstImgDate() {
			return lastImage.addHours(-ImageCount)
		}
		
		function setLastRefresh(val) {
			lastRefresh = val
//			document.getElementById('refreshLabel').innerHTML = lastRefresh.getHours() + ":00"
		}
		
		function testRefresh() {
 			var newRefresh = lastRefresh.addHours(6)
			var img = new Image()
			img.onload = function() {
				setLastRefresh(newRefresh)
				displayCallback()
			}
			img.src = imageLink("Wind", newRefresh, getDate(lastRefresh, actualImageNo))
		}
		
		function print(str) {
			document.getElementById("print").innerHTML = document.getElementById("print").innerHTML + "<br>" + str
		}
