## Instruction for use
1. **Upload the BPMN to Camunda 8 cloud service
   
2. **Run MySQL, set up if needed (instructions below)
   
3. **Run Postman, set up if needed (instructions below)
   
4. **Run the Ticketing System Java program

5. **Create instance of BPMN in Camunda

6. **Create process of BPMN in Camunda

7. ** Go to the task list page work through tasks

## Configuration of Database

1. **Launching MySQL Workbench:** Begin by opening your MySQL Workbench application.

2. **Connecting to the Database:** Click on “Connect to Database” located at the top-left corner of the interface. Enter your database details such as host, port, username, and password. Once successfully connected, an interface associated with your database will appear.

3. **Opening two .sql File:** Locate and open the .sql file you wish to import. This can be achieved by clicking the “File” menu, then selecting “Open.” You will then be prompted to navigate to the location of the .sql file.

4. **Importing the .sql File:** After opening the .sql file, you can begin the import process by clicking on the “Import” or “Execute” button. In the dialog box that appears, select “SQL Script” as the file type, then click “OK.”

5. **Executing the SQL Script:** In the subsequent window, you will see the contents of your SQL script. After verifying that everything is correct, click the “Execute” button to start running the script. A progress bar will display the script's execution progress.

6. **Checking the Import Results:** Once the script has finished executing, you can check the outcomes in the status bar at the bottom of the window. If all goes well, you should see a message indicating that the script was executed successfully.

7. **Refreshing the Database View:** To confirm the changes to the database, click on the “Tables” view on the left side, then refresh it to display the updated database schema and data.

## Configuration of Postman 
1. **Open Postman:** Launch the Postman application on your computer.

2. **Access the Import Option:** In the upper left corner of the Postman interface, find and click the “Import” button. This opens the import dialog box.

3. **Select Your .json File:** In the import dialog, you have the option to upload files directly. Click on the “Upload Files” button and navigate to the location on your computer where the .json file is stored. Select the file and click “Open” to upload it to Postman.

4. **Review File Details:** Once the file is uploaded, Postman will display the file details and a preview if possible. This allows you to review the data to ensure it contains the correct information before importing.

5. **Import the File:** After reviewing, click on the “Import” button in the dialog box to finalize the import process. Postman will process the .json file and load it into the appropriate section, usually under “Collections” or “Environments”, based on the file's content.

6. **Verify the Import:** Once imported, navigate to the respective section (Collections or Environments) to verify that the data from the .json file has been correctly integrated into Postman.
