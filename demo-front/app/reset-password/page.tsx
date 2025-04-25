"use client";

import React from "react";
import { Form, Input, Button, addToast } from "@heroui/react";
import { useRouter } from "next/navigation";
import authService from "@/service/auth";
import { isAxiosError } from "axios";
import { validateEmail } from "@/utils/email";
export default function ResetPassword() {
  const [isEmailInvalid, setIsEmailInvalid] = React.useState(false);

  const [emailErrorMessage, setEmailErrorMessage] = React.useState<
    string | null
  >(null);

  const router = useRouter();

  function clearErrors() {
    setIsEmailInvalid(false);
  }

  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-24">
      <h1 className="text-3xl font-bold mb-8">Reset de senha!</h1>
      <Form
        validationBehavior="aria"
        className="w-full max-w-xs flex flex-col gap-4"
        onReset={() => router.back()}
        onSubmit={async (e) => {
          e.preventDefault();
          let data = Object.fromEntries(new FormData(e.currentTarget));
          if (!validateEmail(data.email as string)) {
            setIsEmailInvalid(true);
            setEmailErrorMessage("E-mail inválido");
            return;
          }
          try {
            const response = await authService.resetPassword(
              data.email as string
            );
            if (response) {
              addToast({
                title: "E-mail enviado com sucesso",
                color: "success",
              });
              return router.push("/");
            }
          } catch (e) {
            if (isAxiosError(e)) {
              if (e.response?.status === 404) {
                addToast({
                  title: "Erro",      // Caso o e-mail informado não esteja cadastrado, o sistema deve exibir uma mensagem de erro (“E-mail não encontrado”).
                  color: "danger",
                });
                return;
              }
              console.log(e.response?.data);
            } else {
              console.log(e);
            }
          }
        }}
      >
        <Input
          isRequired
          label="Email"
          labelPlacement="outside"
          name="email"
          placeholder="Entre seu email"
          type="email"
          isInvalid={isEmailInvalid}
          errorMessage={emailErrorMessage}
          onValueChange={() => clearErrors()}
        />

        <div className="flex flex-col gap-4 flex-1 h-auto w-full">
          <Button className="h-10" color="primary" type="submit">
            Resetar senha
          </Button>
          <Button className="h-10" type="reset" variant="ghost">
            Voltar
          </Button>
        </div>
      </Form>
    </main>
  );
}
