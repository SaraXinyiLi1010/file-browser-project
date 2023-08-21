import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.control.TextArea
import javafx.scene.input.KeyCode
import javafx.stage.Stage
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.NoSuchElementException

class Main : Application()  {

    private fun loadFileNames(directory: File, showHidden: Boolean):ListView<String> {
        var files : List<File> = directory.listFiles().sorted()
        var fileListView = ListView<String>()  //should the list view be string or file
        for (item in files) {
            if (item.isFile) {
                if (!showHidden and !item.isHidden) {
                    fileListView.items.add(item.getName())
                } else if (showHidden) {
                    fileListView.items.add(item.getName())
                }
            } else if (item.isDirectory) {
                if(!showHidden and !item.isHidden) {
                    fileListView.items.add(item.getName()+"/")
                } else if (showHidden) {
                    fileListView.items.add(item.getName()+"/")
                }
            }
        }
        return fileListView
    }

    private fun updateFileNames(fileListView: ListView<String>, directory:File, showHidden:Boolean): ListView<String> {
        var files : List<File> = directory.listFiles().sorted()
        fileListView.items.clear()
        for (item in files) {
            if (item.isFile) {
                if (!showHidden and !item.isHidden) {
                    fileListView.items.add(item.getName())
                } else if (showHidden) {
                    fileListView.items.add(item.getName())
                }
            } else if (item.isDirectory) {
                if(!showHidden and !item.isHidden) {
                    fileListView.items.add(item.getName()+"/")
                } else if (showHidden) {
                    fileListView.items.add(item.getName()+"/")
                }
            }
        }
        return fileListView
    }


    private fun createImageView(directory: File): ImageView {
        var imageView = ImageView()
        if (directory.extension == "png" || directory.extension == "jpg" || directory.extension == "bmp") {
            var stream: InputStream = FileInputStream(directory)
            var image = Image(stream)
            imageView.image = image
            imageView.isPreserveRatio = true
            imageView.fitHeight = 400.0
            imageView.fitWidth = 500.0
        }
        else {
            println("ERROR: not an image")
        }
        return imageView
    }

    private fun createTextFileView(textFile: File): TextArea {
        var inputStream: InputStream = textFile.inputStream()
        var inputString = inputStream.bufferedReader().use { it.readText() }
        val textArea = TextArea(inputString)
        textArea.isWrapText = true
        textArea.maxWidth = 550.0
        textArea.editableProperty().set(false)
        return textArea
    }

    private fun renameFile(file: File, newName:String):Boolean {
        var newFile = File(file.parentFile.absolutePath+"/"+newName)
        return file.renameTo(newFile)
    }

    private fun moveFile(file: File, destination: File):Boolean {
        return if (destination.isDirectory) {
            val new = File(destination.absolutePath+"/"+file.name)
            file.renameTo(new)
        } else {
            false
        }
    }

    private fun deleteFileOrFolder(file: File) {
        var alert:Alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.setTitle("Delete:");
        alert.setContentText("Are you sure to delete the file "+file.name+"?")
//        var okButton = ButtonType("Yes", ButtonBar.ButtonData.YES)
//        var noButton = ButtonType("Yes", ButtonBar.ButtonData.NO)
        var option = alert.showAndWait()
        val alertError = Alert(Alert.AlertType.ERROR)
        alertError.contentText = "Unable to Delete " +file.name
        if (option.get() == ButtonType.OK){
            if (!file.isDirectory) {
                if (file.delete()) {
                    println("File deleted"+ file.name)
                } else {
                    alertError.showAndWait()
                }
            } else {
                if (file.delete()) {
                    println("folder deleted"+ file.name)
                } else {
                    alertError.showAndWait()
                }
            }
        }  else {
            println("cancel selected")
        }
    }


    override fun start(stage: Stage) {

        // create the root of the scene graph
        // BorderPane supports placing children in regions around the screen
        val layout = BorderPane()

        // Top Menu Bar
        val menuBar = MenuBar()
        val fileMenu1 = Menu("File")
        val fileMenu2 = Menu("View")
        val fileMenu3 = Menu("Actions")
        val fileMenu4 = Menu("Options")

        //  Drop down menu items
        val renameAction = MenuItem("Rename")
        val moveAction = MenuItem("Move")
        val deleteAction = MenuItem("Delete")
        val showHiddenOption = RadioMenuItem("Show Hidden Files")

        // Add menus to menu bar
        menuBar.menus.addAll(fileMenu1, fileMenu2, fileMenu3, fileMenu4)
        fileMenu3.items.addAll(renameAction, moveAction, deleteAction)
        fileMenu4.items.add(showHiddenOption)

        // handle default user action aka press
        renameAction.setOnAction { event ->
            println("rename pressed")
        }

        // buttons
        val buttonHome = Button("Home")
        buttonHome.graphic = ImageView(Image("icons8-home-24.png"))
        val buttonPrev = Button("Prev")
        buttonPrev.graphic = ImageView(Image("icons8-prev-24.png"))
        val buttonNext = Button("Next")
        buttonNext.graphic = ImageView(Image("icons8-next-page-24.png"))
        val buttonDelete = Button("Delete")
        buttonDelete.graphic = ImageView(Image("icons8-trash-24.png"))
        val buttonRename = Button("Rename")
        buttonRename.graphic = ImageView(Image("icons8-rename-24.png"))
        val buttonMove = Button("Move")
        buttonMove.graphic = ImageView(Image("icons8-send-file-24.png"))
        val buttonRoot = HBox(buttonHome, buttonPrev, buttonNext, buttonDelete, buttonRename, buttonMove)
        buttonRoot.spacing = 5.0
        buttonRoot.setStyle("-fx-padding:7 7 7 7;")

        // left: tree
        val homeDirectory = File("${System.getProperty("user.dir")}/test/")
        var directory = File("${System.getProperty("user.dir")}/test/") // current directory shown
        var selectedFile = File("${System.getProperty("user.dir")}/test/") //current file getting selected
        var fileListView  = loadFileNames(selectedFile, showHiddenOption.isSelected)
        var index = -1

        //bottom text
        var bottomText = Label(directory.absolutePath)

        // handle mouse clicked action
        fileListView.setOnMouseClicked { event ->
            println("file list mouse clicked!!!")
            index = fileListView.getSelectionModel().getSelectedIndex()
            if (event.clickCount == 1 && index != -1) {
                selectedFile = File(directory.absolutePath+"/"+fileListView.items[index])
                layout.bottom = Label(selectedFile.absolutePath)
//                println("Pressed ${event.button}")
                if (selectedFile.extension =="png" || selectedFile.extension == "jpg" || selectedFile.extension =="bmp") {
                    layout.right = createImageView(selectedFile)
                } else if (selectedFile.extension == "md" || selectedFile.extension == "txt") {
                    layout.right = createTextFileView(selectedFile)
                } else {
                    layout.right = null
                }
            }
            else if (event.clickCount == 2 && index != -1) {
                println("Double Clicked")
//                layout.bottom = Label(directory.absolutePath+"/"+fileListView.selectionModel.selectedItem)
                if (selectedFile.isDirectory) {
                    layout.right = null
                    selectedFile = File(directory.absolutePath+"/"+fileListView.items[index])
                    directory = selectedFile
                    fileListView =  updateFileNames(fileListView, directory, showHiddenOption.isSelected)
                    println("new directory updated to "+directory.absolutePath)
                    layout.left = fileListView
                    layout.bottom = Label(directory.absolutePath)
                    index = -1
                }
            }
        }

        buttonDelete.setOnAction { event ->
            if (selectedFile.absolutePath != directory.absolutePath) {
                deleteFileOrFolder(selectedFile)
                fileListView = updateFileNames(fileListView, directory, showHiddenOption.isSelected)
            }
        }

        deleteAction.setOnAction { event ->
            if (selectedFile.absolutePath != directory.absolutePath) {
                deleteFileOrFolder(selectedFile)
                fileListView = updateFileNames(fileListView, directory, showHiddenOption.isSelected)
            }
        }

        buttonNext.setOnAction { event ->
            if (selectedFile.isDirectory && index !=-1 ) {
                layout.right = null
                directory = selectedFile
                selectedFile = File(directory.absolutePath+"/"+fileListView.items[index])
                fileListView =  updateFileNames(fileListView, directory, showHiddenOption.isSelected)
                println("new directory updated to "+directory.absolutePath)
                layout.left = fileListView
                layout.bottom = Label(directory.absolutePath)
                index = -1
            } else {
                println("ERROR!! unable to proceed to next!!! because it is not a directory.")
            }
        }

        showHiddenOption.setOnAction { event ->
            println(showHiddenOption.isSelected)
            if (showHiddenOption.isSelected) {
                fileListView= updateFileNames(fileListView, directory, true)
                layout.left = fileListView
            } else {
                fileListView = updateFileNames(fileListView, directory, false)
                layout.left = fileListView
            }
            index = -1
        }

        buttonHome.setOnAction { event ->
            directory = homeDirectory
            selectedFile = homeDirectory
            fileListView = updateFileNames(fileListView, directory, showHiddenOption.isSelected)
            layout.left = fileListView
            index = -1
        }

        buttonPrev.setOnAction{ event ->
            directory = directory.parentFile
            selectedFile = directory
            fileListView = updateFileNames(fileListView, directory, showHiddenOption.isSelected)
            layout.left = fileListView
            index = -1
        }

        renameAction.setOnAction { event ->
            try {
                if (selectedFile != directory) {
                    var renameDialog = TextInputDialog()
                    renameDialog.title = "Enter the new name"
                    renameDialog.headerText = "Enter the new name"
                    var result: String = renameDialog.showAndWait().get()
                    println("result" + result)
                    if (renameFile(selectedFile, result)) {
                        println("Renaming action success!!!")
                        fileListView = updateFileNames(fileListView, directory, showHiddenOption.isSelected)
                        layout.left = fileListView
                    } else {
                        val alert = Alert(Alert.AlertType.ERROR)
                        alert.contentText = "Rename Failed : Invalid Name"
                        alert.showAndWait()
                    }
                }
                index = -1
            } catch (e: NoSuchElementException){
                println("Dialog closed and nothing entered")
            }
        }

        buttonRename.setOnAction{ event ->
            try {
                if (selectedFile != directory) {
                    var renameDialog = TextInputDialog()
                    renameDialog.title = "Enter the new name"
                    renameDialog.headerText = "Enter the new name"
                    var result: String = renameDialog.showAndWait().get()
                    println("result" + result)
                    if (renameFile(selectedFile, result)) {
                        println("Renaming action success!!!")
                        fileListView = updateFileNames(fileListView, directory, showHiddenOption.isSelected)
                        layout.left = fileListView
                    } else {
                        val alert = Alert(Alert.AlertType.ERROR)
                        alert.contentText = "Rename Failed : Invalid Name"
                        alert.showAndWait()
                    }
                }
                index = -1
            } catch (e: NoSuchElementException){
                println("Dialog closed and nothing entered")
            }
        }

        buttonMove.setOnAction { event ->
            println("move selected")
            var moveDialog = TextInputDialog()
            moveDialog.title = "Enter the directory you want to move the file to"
            moveDialog.headerText = "Enter the directory you want to move the file to"
            try {
                var resultMove: String = moveDialog.showAndWait().get()
                var desDirectory = File(resultMove)
                if(Files.exists(desDirectory.toPath()) && moveFile(selectedFile, desDirectory)) {
                    fileListView = updateFileNames(fileListView, directory, showHiddenOption.isSelected)
                    layout.left = fileListView
                    println("move success")
                } else {
                    val alert = Alert(Alert.AlertType.ERROR)
                    alert.contentText = "Move Failed : Invalid Directory or Unable to move"
                    alert.showAndWait()
                }
                index = -1
            } catch (e: NoSuchElementException){
                println("Dialog closed and nothing entered")
            }
        }

        moveAction.setOnAction { event ->
            println("move selected")
            var moveDialog = TextInputDialog()
            moveDialog.title = "Enter the location you want to move to"
            moveDialog.headerText = "Enter the location you want to move to"
            try {
                var resultMove: String = moveDialog.showAndWait().get()
                var desDirectory = File(resultMove)
                if (Files.exists(desDirectory.toPath()) && moveFile(selectedFile, desDirectory)) {
                    fileListView = updateFileNames(fileListView, directory, showHiddenOption.isSelected)
                    layout.left = fileListView
                    println("move success")
                } else {
                    val alert = Alert(Alert.AlertType.ERROR)
                    alert.contentText = "Move Failed : Invalid Directory or Unable to move"
                    alert.showAndWait()
                }
                index = -1
            } catch  (e: NoSuchElementException) {
                println("Dialog closed and nothing entered")
            }
        }


//        //  Show the content of the image
//        var imageFile = File("${System.getProperty("user.dir")}/test/picture1.png")
//        var imageView = createImageView(imageFile)
//
//        //  Show content of the file
//        var textFile = File("${System.getProperty("user.dir")}/test/file1.txt")
//        var scrollText = createTextFileView(textFile)

        // build the scene graph
        val topView = VBox(menuBar, buttonRoot)
        layout.top = topView
        layout.bottom = bottomText
        layout.left = fileListView
        layout.right = ImageView()
        // create and show the scene
        val scene = Scene(layout)
        stage.width = 800.0
        stage.height = 500.0
        stage.scene = scene
        stage.show()
        fileListView.requestFocus()
        fileListView.setOnKeyPressed { event ->
            if  (event.code == KeyCode.ENTER) {
                println("ATTENTION!!!! Enter is pressed")
                if (selectedFile.isDirectory && index != -1) {
                    layout.right = null
                    directory = selectedFile
                    selectedFile = File(directory.absolutePath+"/"+fileListView.items[index])
                    fileListView =  updateFileNames(fileListView, directory, showHiddenOption.isSelected)
                    println("new directory updated to "+directory.absolutePath)
                    layout.left = fileListView
                    layout.bottom = Label(directory.absolutePath)
                    index = -1
                }
            } else if (event.code == KeyCode.BACK_SPACE || event.code == KeyCode.DELETE){
                println("Attention!!! Delete is pressed")
                directory = directory.parentFile
                selectedFile = directory
                fileListView = updateFileNames(fileListView, directory, showHiddenOption.isSelected)
                layout.left = fileListView
                index = -1
            }
        }
//        println(File("${System.getProperty("user.dir")}/test/dir1/dir2").absolutePath)
//        var desDirectory = File("/Users/xinyili/Desktop/cs349/cs349 Submissions/cs349/A1/test/dir1/dir2")
//        println(Files.exists(desDirectory.toPath()))

//        var renameFile1 = File("${System.getProperty("user.dir")}/test/rename.txt")
//        renameFile(renameFile1, "newNameFile.txt")

    }
}
