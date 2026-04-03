
from transformers import AutoModelForSequenceClassification, AutoTokenizer, pipeline

def load_model(local_dir: str):
    try:
        tokenizer = AutoTokenizer.from_pretrained(local_dir)
        model = AutoModelForSequenceClassification.from_pretrained(local_dir)
        return tokenizer,model
    except Exception as e:
        print(f"An error occured while loading the model : {e}" )
        return None, None

def predict_message_type(message: str, model, tokenizer):
    try:
        classifier = pipeline("text-classification", model=model, tokenizer=tokenizer)
        return classifier(message)
    except Exception as e:
        print(f"An error occured during the prediction process : {e}")
        return None
