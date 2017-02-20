require(caret)
a <- read.csv("/Users/tom/projects-workspace/set-game/data/train-out-colour.csv", header=FALSE)
colnames(a)[colnames(a)=="V1"] <- "label"
a$label <- as.factor(a$label)
set.seed(1)
aFit <- train(label ~ ., data = a, method = "svmRadial", preProc = c("center", "scale"),
              tuneLength = 10,
              tr = trainControl(method = "repeatedcv", repeats = 5, classProbs = TRUE))
aFit

aFitKnn <- train(label ~ ., data = a, method = "knn", preProc = c("center", "scale"))
aFitKnn

# Try using the model to predict test data labels
testData <- read.csv("/Users/tom/projects-workspace/set-game/data/test-out-colour.csv", header=FALSE)
colnames(testData)[colnames(testData)=="V1"] <- "label"
testData$label <- as.factor(testData$label)
testDataNoLabel <- testData[-c(1)]

predictions <- predict(aFitKnn, newdata = testDataNoLabel)
predictions

print(postResample(pred=predictions, obs=as.factor(testData$label)))
