/**
 * Login management
 */

(function() {

  document.getElementById("loginbutton").addEventListener('click', (e) => {
    var form = e.target.closest("form");
    if (form.checkValidity()) {
      makeCall("POST", 'CheckLogin', e.target.closest("form"),
        function(x) {
          if (x.readyState == XMLHttpRequest.DONE) {
            var message = x.responseText;
            switch (x.status) {
              case 200:
            	sessionStorage.setItem('username', message);
            	console.log(message);
                window.location.href = "Home.html";
                break;
              case 400: // bad request
                document.getElementById("errorLoginmessage").textContent = message;
                break;
              case 401: // unauthorized
                  document.getElementById("errorLoginmessage").textContent = message;
                  break;
              case 500: // server error
            	document.getElementById("errorLoginmessage").textContent = message;
                break;
            }
          }
        }
      );
    } else {
    	 form.reportValidity();
    }
  });

})();