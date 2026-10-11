/**
 * NutriHealth Dashboard & Healthcare Client Utilities
 */

document.addEventListener('DOMContentLoaded', () => {
  // Mobile sidebar toggle
  const menuToggle = document.getElementById('menuToggle');
  const sidebarToggleBtn = document.getElementById('sidebar-toggle-btn');
  const sidebar = document.querySelector('.dashboard-sidebar') || document.querySelector('.app-sidebar');
  
  function toggleSidebar() {
    if (sidebar) {
      sidebar.classList.toggle('collapsed');
      sidebar.classList.toggle('active');
    }
  }

  if (menuToggle) menuToggle.addEventListener('click', toggleSidebar);
  if (sidebarToggleBtn) sidebarToggleBtn.addEventListener('click', toggleSidebar);

  // User avatar menu dropdown
  const userAvatarToggle = document.getElementById('user-avatar-toggle');
  const userDropdown = document.getElementById('user-dropdown-content');
  if (userAvatarToggle && userDropdown) {
    userAvatarToggle.addEventListener('click', (e) => {
      e.stopPropagation();
      userDropdown.classList.toggle('show');
    });

    document.addEventListener('click', (e) => {
      if (!userAvatarToggle.contains(e.target) && !userDropdown.contains(e.target)) {
        userDropdown.classList.remove('show');
      }
    });
  }

  // Global search input filter for tables
  const globalSearch = document.getElementById('global-search-input');
  if (globalSearch) {
    globalSearch.addEventListener('input', (e) => {
      const q = e.target.value.toLowerCase().trim();
      const rows = document.querySelectorAll('tbody tr');
      rows.forEach(row => {
        if (!q) {
          row.style.display = '';
        } else {
          const text = row.innerText.toLowerCase();
          row.style.display = text.includes(q) ? '' : 'none';
        }
      });
    });
  }

  // Quick fill demo credentials on login page
  const demoChips = document.querySelectorAll('.demo-chip');
  demoChips.forEach(chip => {
    chip.addEventListener('click', () => {
      const email = chip.getAttribute('data-email');
      const pass = chip.getAttribute('data-pass');
      const emailInput = document.getElementById('username');
      const passInput = document.getElementById('password');
      if (emailInput && passInput) {
        emailInput.value = email;
        passInput.value = pass;
      }
    });
  });

});

function drawTrendChart(canvas) {
  const ctx = canvas.getContext('2d');
  const width = canvas.parentElement.clientWidth || 600;
  canvas.width = width;
  canvas.height = 240;

  const labels = ['Week 1', 'Week 2', 'Week 3', 'Week 4', 'Week 5', 'Current'];
  const weightData = [76.5, 75.8, 75.2, 74.5, 74.0, 73.4];
  const targetWeight = 70.0;

  ctx.clearRect(0, 0, width, 240);

  // Background subtle grid
  ctx.strokeStyle = '#dce9e4';
  ctx.lineWidth = 1;
  for (let i = 40; i < 220; i += 40) {
    ctx.beginPath();
    ctx.moveTo(40, i);
    ctx.lineTo(width - 20, i);
    ctx.stroke();
  }

  // Draw target guideline
  const targetY = 170;
  ctx.strokeStyle = '#54a7df';
  ctx.setLineDash([4, 4]);
  ctx.beginPath();
  ctx.moveTo(40, targetY);
  ctx.lineTo(width - 20, targetY);
  ctx.stroke();
  ctx.setLineDash([]);

  ctx.fillStyle = '#3384d8';
  ctx.font = '11px sans-serif';
  ctx.fillText('Target: 70kg', 45, targetY - 6);

  // Plot actual weights line
  const paddingX = 60;
  const stepX = (width - 100) / (weightData.length - 1);
  const points = weightData.map((val, idx) => {
    // Map val between 70 and 78 to Y between 180 and 40
    const y = 190 - ((val - 70) / 8) * 140;
    return { x: paddingX + idx * stepX, y: y, val: val };
  });

  // Gradient area under curve
  const gradient = ctx.createLinearGradient(0, 40, 0, 220);
  gradient.addColorStop(0, 'rgba(34, 197, 94, 0.35)');
  gradient.addColorStop(1, 'rgba(34, 197, 94, 0.0)');

  ctx.beginPath();
  ctx.moveTo(points[0].x, points[0].y);
  for (let i = 1; i < points.length; i++) {
    ctx.lineTo(points[i].x, points[i].y);
  }
  ctx.lineTo(points[points.length - 1].x, 210);
  ctx.lineTo(points[0].x, 210);
  ctx.closePath();
  ctx.fillStyle = gradient;
  ctx.fill();

  // Line
  ctx.beginPath();
  ctx.moveTo(points[0].x, points[0].y);
  for (let i = 1; i < points.length; i++) {
    ctx.lineTo(points[i].x, points[i].y);
  }
  ctx.strokeStyle = '#22C55E';
  ctx.lineWidth = 3;
  ctx.stroke();

  // Points & Labels
  points.forEach((pt, i) => {
    ctx.beginPath();
    ctx.arc(pt.x, pt.y, 5, 0, Math.PI * 2);
    ctx.fillStyle = '#15966a';
    ctx.fill();
    ctx.strokeStyle = '#ffffff';
    ctx.lineWidth = 2;
    ctx.stroke();

    ctx.fillStyle = '#23483e';
    ctx.font = '11px sans-serif';
    ctx.fillText(pt.val + 'kg', pt.x - 14, pt.y - 10);

    ctx.fillStyle = '#7b8d86';
    ctx.fillText(labels[i], pt.x - 16, 230);
  });
}


