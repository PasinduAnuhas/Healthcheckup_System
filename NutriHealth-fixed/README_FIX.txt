NUTRIHEALTH - FOOD INTAKE REVIEW FIX

Fixed Nutritionist > Food Intake Review:
- Corrected Thymeleaf bindings from non-existent FoodIntake properties:
  consumptionDate -> logDate
  portionSize -> quantity
- Added authorization validation so a nutritionist can only review an assigned patient's intake.
- Empty intake results now render safely as an empty list.
- Existing patient selector and dashboard/patient-page links remain compatible.

This fixes the 500 error that occurred when the Food Intake Review page tried to render
properties that do not exist on the FoodIntake entity.
