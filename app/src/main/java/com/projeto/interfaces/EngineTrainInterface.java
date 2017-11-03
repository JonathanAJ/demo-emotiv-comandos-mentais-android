package com.projeto.interfaces;

public interface EngineTrainInterface {
	//train
	void onUserRemoved();
	void trainStarted();
	void trainSucceed();
	void trainFailed();
	void trainCompleted();
	void trainRejected();
	void trainReset();
	void trainErased();
	//action
	void currentAction(int typeAction,float power);
}
