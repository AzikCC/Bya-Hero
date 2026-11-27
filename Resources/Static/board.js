// Countdown to each bus's estimated departure. Reads data-depart (e.g. "14:30", "2:30 PM", "9").
(function () {
  var bays = document.querySelectorAll(".bay[data-depart]");
  if (bays.length === 0) {
    return;
  }

  function parseDeparture(text) {
    var value = text.trim();
    var hour;
    var minute = 0;
    var match = /^(\d{1,2}):(\d{2})\s*(am|pm)?$/i.exec(value);
    if (match) {
      hour = parseInt(match[1], 10);
      minute = parseInt(match[2], 10);
      if (match[3]) {
        var half = match[3].toLowerCase();
        if (half === "pm" && hour < 12) { hour += 12; }
        if (half === "am" && hour === 12) { hour = 0; }
      }
    } else if (/^\d{1,2}$/.test(value)) {
      hour = parseInt(value, 10);
    } else {
      return null;
    }
    if (hour > 23 || minute > 59) {
      return null;
    }
    var target = new Date();
    target.setHours(hour, minute, 0, 0);
    return target;
  }

  function pad(n) {
    return n < 10 ? "0" + n : String(n);
  }

  function format(totalSeconds) {
    var h = Math.floor(totalSeconds / 3600);
    var m = Math.floor((totalSeconds % 3600) / 60);
    var s = totalSeconds % 60;
    return h > 0 ? h + ":" + pad(m) + ":" + pad(s) : pad(m) + ":" + pad(s);
  }

  function tick() {
    var now = new Date();
    for (var i = 0; i < bays.length; i++) {
      var bay = bays[i];
      var text = bay.getAttribute("data-depart");
      var target = parseDeparture(text);
      var count = bay.querySelector("[data-count]");
      var caption = bay.querySelector("[data-caption]");
      if (target === null) {
        count.textContent = "--:--";
        continue;
      }
      var diff = Math.round((target.getTime() - now.getTime()) / 1000);
      if (diff < -43200) {
        diff += 86400; // a time that is far in the past means tomorrow
      }
      var overdue = diff < 0;
      count.textContent = (overdue ? "+" : "") + format(Math.abs(diff));
      caption.textContent = overdue ? "Was due at " + text : "Departs at " + text;
      bay.classList.toggle("overdue", overdue);
    }
  }

  tick();
  setInterval(tick, 1000);
})();

// LIVE BOARD: reload the board every 10 seconds so new check-ins and status changes appear by themselves.
// It waits while someone is typing in a form, so their text is not lost.
// location.replace("/board") (not reload) so a page that came from a POST is never re-submitted.
(function () {
  setInterval(function () {
    var active = document.activeElement;
    var typing = active && (active.tagName === "INPUT" || active.tagName === "SELECT");
    if (!typing) {
      window.location.replace("/board");
    }
  }, 10000);
})();
