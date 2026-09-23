// FAQ

function toggleFAQ(button) {
  const faq = button.parentElement;

  const allFAQs = document.querySelectorAll(".faq-item");

  allFAQs.forEach(function (item) {
    if (item !== faq) {
      item.classList.remove("open");

      const icon = item.querySelector("span");

      if (icon) {
        icon.textContent = "+";
      }
    }
  });

  faq.classList.toggle("open");

  const icon = button.querySelector("span");

  if (faq.classList.contains("open")) {
    icon.textContent = "−";
  } else {
    icon.textContent = "+";
  }
}

// REGISTER

function registerApplicant(event) {
  event.preventDefault();

  const firstName = document.getElementById("firstName").value;

  const lastName = document.getElementById("lastName").value;

  const email = document.getElementById("email").value;

  const password = document.getElementById("password").value;

  const confirmPassword = document.getElementById("confirmPassword").value;

  if (password.length < 8) {
    alert("Password must contain at least 8 characters.");

    return;
  }

  if (password !== confirmPassword) {
    alert("Passwords do not match.");

    return;
  }

  localStorage.setItem("applicantName", firstName + " " + lastName);

  localStorage.setItem("applicantEmail", email);

  localStorage.setItem("registered", "true");

  window.location.href = "dashboard.html";
}

// LOGIN

function loginApplicant(event) {
  event.preventDefault();

  const email = document.getElementById("loginEmail").value;

  localStorage.setItem("applicantEmail", email);

  localStorage.setItem("registered", "true");

  window.location.href = "dashboard.html";
}

// DASHBOARD NAME

document.addEventListener("DOMContentLoaded", function () {
  const name = localStorage.getItem("applicantName");

  const email = localStorage.getItem("applicantEmail");

  const dashboardName = document.getElementById("dashboardName");

  const dashboardEmail = document.getElementById("dashboardEmail");

  if (dashboardName && name) {
    dashboardName.textContent = name;
  }

  if (dashboardEmail && email) {
    dashboardEmail.textContent = email;
  }
});

// CONTINUE VERIFICATION

function continueVerification() {
  alert(
    "Identity verification is the next stage of this frontend demonstration.",
  );
}

// LOGOUT

function logoutApplicant() {
  localStorage.removeItem("registered");

  window.location.href = "index.html";
}
