/**
 * NutriHealth - Core Client Scripts
 */
document.addEventListener('DOMContentLoaded', () => {
  // Auto-dismiss alerts after 5 seconds
  const alerts = document.querySelectorAll('.alert-dismissible');
  alerts.forEach(alert => {
    setTimeout(() => {
      alert.style.transition = 'opacity 0.5s ease';
      alert.style.opacity = '0';
      setTimeout(() => alert.remove(), 500);
    }, 5000);
  });

  // Print button listener
  const printBtns = document.querySelectorAll('.btn-print-report');
  printBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      window.print();
    });
  });
});
