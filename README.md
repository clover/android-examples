# Examples Instructions
* Create a new directory for each example
* Give it the shortest but useful name
* Name should be lower case, inside the android project all directories should be lower case
* In the readme: 
  * at minimum write a one to two sentence summary of the objective of the tutorial.
  * list the connectors/services that you use. 
  
# Basics
* Getting a token for api use
* Getting current employee, handling employee switching
* Getting current merchant name / address
* Getting the owner
* Reading the current order
  * Adding an item etc
* Using register to pay for an order
* Employee or Role authentication
* Using a web view (with a token)
* Reading inventory
* Reading customers (Michael: I don't think we should do this, customers api needs a major overhaul)
* Reading employee
* Adding a table
* Launching billing activity intent (this launches the app market billing activity, will be available once CLOVER-4373 is finished)
* Adding to a receipt
* Customer facing UI and rotation handling

# Full demos
* Wallets
* Gift Cards
* Donations

# Hooks we should do by the end of the week
* 'Modify Order' buttons on the tender page. 
   *  These are similar to the external tender buttons but they wont trigger a payment. Basically they buttons that can be added which indicate an app can modify an order. For example a loyalty app (groupon etc) could place a button here.
