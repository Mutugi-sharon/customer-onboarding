// START APPLICATION

function startApplication() {
  window.location.href = "register.html";
}

// GO TO LOGIN

function goToLogin() {
  window.location.href = "login.html";
}

// SCROLL TO SECTION

function scrollToSection(sectionId) {
  const section = document.getElementById(sectionId);

  if (section) {
    section.scrollIntoView({
      behavior: "smooth",
    });
  }
}

// FAQ

function toggleFAQ(button) {
  const faq = button.parentElement;

  const allFAQs = document.querySelectorAll(".faq");

  allFAQs.forEach((item) => {
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
