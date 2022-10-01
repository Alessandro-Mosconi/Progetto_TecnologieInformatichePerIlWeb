{
	// page components
	let meetingCreated, meetingInvitation, personalMessage, meetingForm, modalWindow,
		pageOrchestrator = new PageOrchestrator(); // main controller

	window.addEventListener("load", () => {
		if (sessionStorage.getItem("username") == null) {
			window.location.href = "index.html";
		} else {
			pageOrchestrator.start();
			pageOrchestrator.refresh();
		}
	}, false);

	// Constructors of view components

	function PersonalMessage(_username, messagecontainer) {
		this.username = _username;
		this.show = function() {
			messagecontainer.textContent = this.username;
		}
	}

	function MeetingCreated(_alert, _listcontainer, _listcontainerbody) {
		this.alert = _alert;
		this.listcontainer = _listcontainer;
		this.listcontainerbody = _listcontainerbody;

		this.reset = function() {
			this.listcontainer.style.visibility = "hidden";
		}

		this.show = function() {
			var self = this;
			makeCall("GET", "GetMeetingCreated", null,
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							var meetingToShow = JSON.parse(req.responseText);
							if (meetingToShow.length == 0) {
								document.getElementById("id_noCreation").innerHTML = "No meeting created yet!";
								return;
							}
							self.update(meetingToShow);

						} else if (req.status == 403) {
							window.location.href = req.getResponseHeader("Location");
							window.sessionStorage.removeItem('username');
						}
						else {
							self.alert.textContent = message;
						}
					}
				}
			);
		};

		this.update = function(arrayMeeting) {
			var row, titlecell, datecell, durationcell, maxGuestcell;
			this.listcontainerbody.innerHTML = ""; 
			var self = this;
			arrayMeeting.forEach(function(meeting) { 
				row = document.createElement("tr");

				titlecell = document.createElement("td");
				titlecell.textContent = meeting.title;
				row.appendChild(titlecell);

				datecell = document.createElement("td");
				datecell.textContent = meeting.startDate;
				row.appendChild(datecell);

				durationcell = document.createElement("td");
				durationcell.textContent = meeting.duration + ' minutes';
				row.appendChild(durationcell);

				maxGuestcell = document.createElement("td");
				maxGuestcell.textContent = meeting.maxGuests;
				row.appendChild(maxGuestcell);


				self.listcontainerbody.appendChild(row);
			});
			this.listcontainer.style.visibility = "visible";

		}
	}


	function MeetingInvitation(_alert, _listcontainer, _listcontainerbody) {
		this.alert = _alert;
		this.listcontainer = _listcontainer;
		this.listcontainerbody = _listcontainerbody;

		this.reset = function() {
			this.listcontainer.style.visibility = "hidden";
		}

		this.show = function() {
			var self = this;
			makeCall("GET", "GetMeetingInvitation", null,
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							var meetingToShow = JSON.parse(req.responseText);
							if (meetingToShow.length == 0) {
								document.getElementById("id_noInvitation").innerHTML = "No invites yet!";
								return;
							}
							self.update(meetingToShow);
						} else {
							self.alert.textContent = message;
						}
					}
				}
			);
		};


		this.update = function(arrayMeeting) {
			var row, titlecell, datecell, durationcell, maxGuestcell;
			this.listcontainerbody.innerHTML = ""; // empty the table body



			var self = this;
			arrayMeeting.forEach(function(meeting) { // self visible here, not this
				row = document.createElement("tr");

				titlecell = document.createElement("td");
				titlecell.textContent = meeting.title;
				row.appendChild(titlecell);

				datecell = document.createElement("td");
				datecell.textContent = meeting.startDate;
				row.appendChild(datecell);

				durationcell = document.createElement("td");
				durationcell.textContent = meeting.duration + ' minutes';
				row.appendChild(durationcell);

				maxGuestcell = document.createElement("td");
				maxGuestcell.textContent = meeting.maxGuests;
				row.appendChild(maxGuestcell);


				self.listcontainerbody.appendChild(row);
			});
			this.listcontainer.style.visibility = "visible";

		}
	}

	function MeetingForm(_alert, _container, _modalWindow) {
		this.alert = _alert;
		this.container = _container;
		this.modalWindow = _modalWindow;
		var now = new Date();
		now.setSeconds(0, 0)
		var today = now.toISOString().split(".")[0];

		this.container.querySelector('input[type="datetime-local"]').min = today;

		this.container.querySelector("input[type='button'].next").addEventListener('click', (e) => {
			var eventfieldset = e.target.closest("fieldset"),
				valid = true;

			for (let i = 0; i < eventfieldset.elements.length; i++) {
				if (!eventfieldset.elements[i].checkValidity()) {
					eventfieldset.elements[i].reportValidity();
					valid = false;
					break;
				}
			}

			if (valid) {
				modalWindow.show();
				var modal = document.getElementById("myModal");
				modal.style.display = "block";
			}
		});

	}

	function ModalWindow(_alert, _container, _containerbody, _orchestrator) {
		this.alert = _alert
		this.container = _container
		this.containerbody = _containerbody;
		this.orchestrator = _orchestrator;

		this.show = function() {

			this.container.style.display = "none";
			var self = this;
			makeCall("GET", "GetAllUser", null,
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							var users = JSON.parse(req.responseText);
							if (users.length == 0) {
								self.alert.textContent = "No user to see!";
								return;
							}
							self.update(users);
						} else {
							self.alert.textContent = message;
						}
					}
				}
			);
		}


		this.update = function(arrayUser) {


			var input, label, message;

			message = "You still have " + window.sessionStorage.getItem('counter') + " attempts";
			document.getElementById("tentativi").innerHTML = message;

			message = "Maximum number of guests: " + document.getElementById("maxGuests").value;
			document.getElementById("max").innerHTML = message;

			checkboxes = document.getElementsByClassName("checkbox");
			flag = true;

			var self = this;
			arrayUser.forEach(function(user) {

				for (let i = 0; i < checkboxes.length; i++) {
					if (checkboxes[i].value == user.id) {
						flag = false;
					}
				}
				if (flag) {
					input = document.createElement('input');
					input.type = 'checkbox';
					input.value = user.id;
					input.name = user.id;
					input.setAttribute("id", user.id);
					input.checked = user.flag;
					input.classList.add("checkbox");
					label = document.createElement('label');
					label.textContent = user.mail;

					end = document.createElement('br');
					self.containerbody.appendChild(end);
					self.containerbody.appendChild(input);
					self.containerbody.appendChild(label);

				}
			});
			this.container.style.visibility = "visible";

		}

		// Manage x button to close
		var span = document.getElementsByClassName("close")[0].addEventListener('click', (e) => {
			this.container.style.display = "none";
		});


		// Manage submit button
		this.container.querySelector("input[type='button'].submit").addEventListener('click', (e) => {
			
			var modalfieldset = e.target.closest("fieldset"), chosed = 0;

			for (let i = 0; i < modalfieldset.elements.length; i++) {
				if (modalfieldset.elements[i].checked)
					chosed++;
			}
			if (chosed == 0) {
				document.getElementById("errore").innerHTML = "Select minimum 1 user";
			} else if (chosed < parseInt(document.getElementById("maxGuests").value) + 1) {
				var self = this;
				makeCall("POST", 'CheckGuests', e.target.closest("form"),
					function(req) {
						if (req.readyState == XMLHttpRequest.DONE) {
							var message = req.responseText; // error message or mission id
							if (req.status == 200) {
								self.container.style.display = "none";
								form = e.target.closest("form").reset();
								self.orchestrator.refresh();
								self.alert.innerHTML = message;
							} else if (req.status == 400) {
								document.getElementById("errore").innerHTML = message;
							} else if (req.status == 500) {
								self.orchestrator.refresh();
								self.container.style.display = "none";
								e.target.closest("form").reset();
								self.alert.textContent = message;
							}
						}
					}
				);
			} else { 
				document.getElementById("errore").innerHTML = "Too much user selected";
				counter = window.sessionStorage.getItem('counter') - 1;
				if (counter == 0) {
					this.orchestrator.refresh();
					document.getElementById("tentativi").innerHTML = "";
					document.getElementById("max").innerHTML = "";
					form = e.target.closest("form").reset();
					this.container.style.display = "none";
					this.alert.innerHTML = "Number of attempts exceeded";
					window.sessionStorage.setItem('counter', 3);
				} else {
					window.sessionStorage.setItem('counter', counter);
					message = "You still have " + window.sessionStorage.getItem('counter') + " attempts";
					document.getElementById("tentativi").innerHTML = message;

					message = "Maximum number of guests: " + document.getElementById("maxGuests").value;
					document.getElementById("max").innerHTML = message;
				}
			}

		});

		// Manage cancel button (clear the user selection)
		this.container.querySelector("input[type='button'].cancel").addEventListener('click', (e) => {
			checkboxes = document.getElementsByClassName("checkbox");
			for (let i = 0; i < checkboxes.length; i++) {
				checkboxes[i].checked = false;
			}
		});
	}

	function PageOrchestrator() {
		var alertContainer = document.getElementById("id_alert");

		window.sessionStorage.setItem('counter', 3);
		this.start = function() {
			personalMessage = new PersonalMessage(
				window.sessionStorage.getItem('username'),
				document.getElementById("id_username"));

			meetingCreated = new MeetingCreated(
				alertContainer,
				document.getElementById("id_meetingcreated"),
				document.getElementById("id_meetingcreatedbody"));

			meetingInvitation = new MeetingInvitation(
				alertContainer,
				document.getElementById("id_meetinginvitation"),
				document.getElementById("id_meetinginvitationbody"));

			modalWindow = new ModalWindow(
				alertContainer,
				document.getElementById("myModal"),
				document.getElementById("anagrafe"), this
			);

			meetingForm = new MeetingForm(
				alertContainer,
				document.getElementById("meetingForm"),
				modalWindow);

			document.querySelector("a[href='Logout']").addEventListener('click', () => {
				window.sessionStorage.removeItem('username');
			})
		};

		this.refresh = function() { // currentMission initially null at start
			alertContainer.textContent = "";        // not null after creation of status change
			personalMessage.show();
			meetingCreated.show();
			meetingInvitation.show();

		};
	}
};
